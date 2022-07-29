package com.bh.planners.core.kether.compat.germplugin

import com.bh.planners.core.effect.Target
import com.bh.planners.core.kether.*
import com.bh.planners.core.kether.compat.adyeshach.AdyeshachEntity
import com.germ.germplugin.api.GermPacketAPI
import com.germ.germplugin.api.GermSrcManager
import com.germ.germplugin.api.RootType
import com.germ.germplugin.api.SoundType
import com.germ.germplugin.api.dynamic.effect.GermEffectParticle
import com.germ.germplugin.api.event.GermSrcPreReloadEvent
import com.germ.germplugin.api.event.GermSrcReloadEvent
import ink.ptms.adyeshach.common.entity.EntityInstance
import ink.ptms.adyeshach.common.entity.manager.Manager
import ink.ptms.adyeshach.common.script.ScriptHandler.getEntities
import org.bukkit.Bukkit
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Entity
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.platform.function.info
import taboolib.common.util.random
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.Collections
import java.util.UUID
import java.util.concurrent.CompletableFuture

class ActionGermEngine {

    class ActionAnimation(val state: String, val remove: Boolean, val selector: ParsedAction<*>) :
        ScriptAction<Void>() {

        fun execute(entity: Entity, state: String, remove: Boolean) {
            Bukkit.getOnlinePlayers().forEach {
                if (remove) {
                    GermPacketAPI.stopModelAnimation(it, entity.entityId, state)
                } else {
                    GermPacketAPI.sendModelAnimation(it, entity.entityId, state)
                }
            }
        }

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            frame.execEntity(selector) { execute(this, state, remove) }
            return CompletableFuture.completedFuture(null)
        }
    }

    class ActionSound(
        val name: ParsedAction<*>,
        val type: ParsedAction<*>,
        val volume: ParsedAction<*>,
        val pitch: ParsedAction<*>,
        val selector: ParsedAction<*>
    ) :
        ScriptAction<Void>() {


        override fun run(frame: ScriptFrame): CompletableFuture<Void> {

            frame.runTransfer<String>(name) { name ->
                frame.runTransfer<SoundType>(type) { type ->
                    frame.runTransfer<Float>(volume) { volume ->
                        frame.runTransfer<Float>(pitch) { pitch ->
                            frame.execLocation(selector) {
                                GermPacketAPI.playSound(this, name, type, 0, volume, pitch)
                            }
                        }
                    }
                }
            }

            return CompletableFuture.completedFuture(null)
        }
    }

    class ActionParticle(val name: ParsedAction<*>, val selector: ParsedAction<*>?) : ScriptAction<Void>() {

        companion object {

            private val cache = Collections.synchronizedMap(mutableMapOf<String, ConfigurationSection>())


            private fun get(name: String): ConfigurationSection? {
                return cache.computeIfAbsent(name) {
                    val split = name.split(":")
                    GermSrcManager.getGermSrcManager().getSrc(split[0], RootType.EFFECT)
                        ?.getConfigurationSection(split[1])
                }
            }

            private fun create(name: String): GermEffectParticle {
                return GermEffectParticle.getGermEffectPart(
                    UUID.randomUUID().toString(),
                    get(name) ?: error("GermPlugin effect '$name' not found.")
                ) as GermEffectParticle
            }

            @SubscribeEvent
            private fun e(e: GermSrcReloadEvent) {
                cache.clear()
            }

        }

        fun execute(target: Target, effect: GermEffectParticle) {
            Bukkit.getOnlinePlayers().forEach {
                if (target is Target.Entity) {
                    effect.spawnToEntity(it, target.entity)
                } else if (target is Target.Location) {
                    effect.spawnToLocation(it, target.value)
                }
            }
        }

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            frame.runTransfer<String>(name) { name ->
                val effectParticle = create(name)
                if (selector != null) {
                    frame.createContainer(selector).thenAccept {
                        it.targets.forEach { execute(it, effectParticle) }
                    }
                } else {
                    execute(frame.toOriginLocation()!!, effectParticle)
                }
            }
            return CompletableFuture.completedFuture(null)
        }


    }

    companion object {

        /**
         * germ animation send [name: token] [selector]
         *
         * germ animation stop [name: token] [selector]
         *
         * 音效播放
         * germ sound name <type: action(master)> <volume: action(1)> <pitch: action(1)> <selector>
         *
         * germ sound name type master volume 1.0 pitch 1.0 they "-@self"
         *
         * 例子播放
         * germ effect [name: action] <selector>
         */
        @KetherParser(["germengine", "germ", "germplugin"], namespace = NAMESPACE, shared = true)
        fun parser() = scriptParser {
            it.switch {
                case("animation") {
                    when (it.expects("send", "stop")) {
                        "send" -> {
                            ActionAnimation(it.nextToken(), false, it.selector())
                        }

                        "stop" -> {
                            ActionAnimation(it.nextToken(), true, it.selector())
                        }

                        else -> error("out of case")
                    }
                }
                case("sound") {
                    ActionSound(
                        it.next(ArgTypes.ACTION),
                        it.tryGet(arrayOf("soundtype", "type"), "MASTER")!!,
                        it.tryGet(arrayOf("volume"), 1)!!,
                        it.tryGet(arrayOf("pitch"), 1)!!,
                        it.selector()
                    )
                }
                case("effect") {
                    ActionParticle(it.next(ArgTypes.ACTION), it.selectorAction())
                }
            }
        }
    }
}