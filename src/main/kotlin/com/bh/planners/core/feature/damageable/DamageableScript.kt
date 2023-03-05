package com.bh.planners.core.feature.damageable

import com.bh.planners.core.effect.Target
import com.bh.planners.core.effect.Target.Companion.isPlayer
import com.bh.planners.core.effect.Target.Companion.toTarget
import com.bh.planners.core.kether.rootVariables
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.kether.KetherShell
import taboolib.module.kether.ScriptContext
import taboolib.module.kether.runKether
import java.util.concurrent.CompletableFuture

object DamageableScript {

    val NAMESPACE = "damageable"
    val NAMESPACES = listOf(NAMESPACE)

    fun createScriptStream(context: Damageable,stream: DamageableModel.Stream): CompletableFuture<DamageableMeta> {
        val meta = DamageableMeta(context, stream)
        if (stream.data != null) {
            return invokeScript(stream.data,meta.sender.toTarget()) {
                this.setDamageable(context)
                this.setDamageableMeta(meta)
            }.thenApply {
                meta.data = it
                meta
            }
        }
        return CompletableFuture.completedFuture(meta)
    }

    fun invokeScript(script: String, sender: Target.Entity,block: ScriptContext.() -> Unit = {}): CompletableFuture<Any?> {
        return runKether {
            KetherShell.eval(script, namespace = NAMESPACES) {
                val entity = sender.bukkitLivingEntity!!
                if (entity is Player) {
                    this.sender = adaptPlayer(entity)
                }
                block()
            }
        } ?: CompletableFuture.completedFuture(null)
    }

}