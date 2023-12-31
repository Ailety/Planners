package com.bh.planners.core.kether.game

import com.bh.planners.api.common.Demand
import com.bh.planners.api.event.EntityEvents
import com.bh.planners.core.effect.Target.Companion.getLivingEntity
import com.bh.planners.core.feature.damageable.DamageableDispatcher
import com.bh.planners.core.kether.*
import com.bh.planners.core.kether.game.damage.AttackProvider
import com.bh.planners.core.kether.game.damage.DamageType
import com.bh.planners.util.eval
import org.bukkit.attribute.Attribute
import org.bukkit.entity.LivingEntity
import org.bukkit.metadata.FixedMetadataValue
import taboolib.common.platform.function.submit
import taboolib.library.kether.ParsedAction
import taboolib.library.reflex.Reflex.Companion.getProperty
import taboolib.library.reflex.Reflex.Companion.setProperty
import taboolib.module.kether.*
import taboolib.module.nms.MinecraftVersion
import taboolib.platform.BukkitPlugin
import taboolib.platform.util.removeMeta
import taboolib.platform.util.setMeta
import java.util.concurrent.CompletableFuture

class ActionDamage {

    class Damage(
        val value: ParsedAction<*>,
        val selector: ParsedAction<*>,
        val source: ParsedAction<*>?,
        val type: ParsedAction<*>
    ) : ScriptAction<Void>() {

        fun execute(entity: LivingEntity, source: LivingEntity?, damage: String, type: DamageType) {
            val result = damage.eval(entity.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 0.0)
            val damageByEntityEvent = EntityEvents.DamageByEntity(source, entity, result, type)
            if (damageByEntityEvent.call()) {
                doDamage(source, entity, damageByEntityEvent.value)
            }
        }

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            frame.run(value).str { damage ->
                frame.container(selector).thenAccept { victims ->
                    frame.containerOrSender(source).thenAccept { source ->
                        frame.run(type).str last@{ type ->
                            val sourceEntity = source.firstLivingEntityTarget() ?: return@last
                            victims.forEachLivingEntity {
                                execute(this, sourceEntity, damage, DamageType.valueOf(type.uppercase()))
                            }
                        }
                    }
                }
            }

            return CompletableFuture.completedFuture(null)
        }
    }

    class Attack(val value: ParsedAction<*>, var data: ParsedAction<*>, val selector: ParsedAction<*>) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            val source = frame.getContext().sender.getLivingEntity() ?: return CompletableFuture.completedFuture(null)
            frame.run(value).str { damage ->
                frame.run(data).str { data ->
                    val demand = Demand(data)
                    frame.container(selector).thenAccept { container ->
                        val damageableModelId = demand.namespace
                        submit {
                            container.forEachLivingEntity {
                                // 跳转到战斗模型
                                if (damageableModelId != "EMPTY") {
                                    DamageableDispatcher.submitDamageable(damageableModelId, source, this, demand)
                                }
                                // 默认攻击
                                else {
                                    this.noDamageTicks = 0
                                    this.setMetadata(
                                        "Planners:Attack",
                                        FixedMetadataValue(BukkitPlugin.getInstance(), true)
                                    )
                                    AttackProvider.INSTANCE?.doDamage(this, damage.eval(this.getAttribute(Attribute.GENERIC_MAX_HEALTH)?.value ?: 0.0), source, demand)
                                    this.setMetadata(
                                        "Planners:Attack",
                                        FixedMetadataValue(BukkitPlugin.getInstance(), false)
                                    )
                                }

                            }
                        }
                    }
                }
            }
            return CompletableFuture.completedFuture(null)
        }
    }

    companion object {

        /**
         * 对selector目标造成伤害
         * damage [damage] [selector]
         * damage 10.0 they ":@aline 10" source ":@self" type "PHYSICS"
         */
        @KetherParser(["damage"], namespace = NAMESPACE, shared = true)
        fun parser() = scriptParser {
            Damage(
                it.nextParsedAction(),
                it.nextSelector(),
                it.nextOptionalParsedAction(arrayOf("source")),
                it.nextOptionalParsedAction(arrayOf("type"), "MAGIC")!!
            )
        }

        /**
         * 对selector目标攻击,
         * attack [damage] [selector]
         * attack 10.0 they "@aline 10"
         */
        @KetherParser(["attack"], namespace = NAMESPACE, shared = true)
        fun parser2() = scriptParser {
            val action = it.nextParsedAction()
            Attack(
                action,
                it.nextOptionalParsedAction(arrayOf("option", "data", "opt"), "EMPTY")!!,
                it.nextSelector()
            )
        }

        fun doDamage(source: LivingEntity?, entity: LivingEntity, damage: Double) {
            entity.noDamageTicks = 0
            entity.setMeta("Planners:Damage", true)

            // 如果实体血量 - 预计伤害值 < 0 提前设置击杀者
            if (source != null && entity.health - damage <= 0) {
                entity.setKiller(source)
//                EntityDeathEvent(entity, emptyList())
            }
            entity.damage(damage)
            entity.removeMeta("Planners:Damage")
        }

        fun LivingEntity.setKiller(source: LivingEntity) {
            when (MinecraftVersion.major) {
                // 1.12.* 1.16.*
                4, 8 -> setProperty("entity/killer", source.getProperty("entity"))
                // 1.15.* 1.17.* bc
                7, 9 -> setProperty("entity/bc", source.getProperty("entity"))
                // 1.18.2 bc 1.18.1 bd
                10 -> if (MinecraftVersion.minecraftVersion == "v1_18_R2") {
                    setProperty("entity/bc", source.getProperty("entity"))
                } else {
                    setProperty("entity/bd", source.getProperty("entity"))
                }
                // 1.18.* 1.19.* bd
                11 -> setProperty("entity/bd", source.getProperty("entity"))

            }
        }

    }

}