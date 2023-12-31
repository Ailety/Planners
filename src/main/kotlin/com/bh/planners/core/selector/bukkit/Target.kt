package com.bh.planners.core.selector.bukkit

import com.bh.planners.core.effect.Target.Companion.getLivingEntity
import com.bh.planners.core.effect.Target.Companion.toTarget
import com.bh.planners.core.effect.createAwaitVoidFuture
import com.bh.planners.core.selector.Selector
import org.bukkit.Material
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import java.util.concurrent.CompletableFuture

object Target : Selector {

    /**
     * 视角所看向的实体
     * @target [距离] [被墙阻隔] [只取远点]
     * @target 10 true false
     * @target 13 false true
     */

    override val names: Array<String>
        get() = arrayOf("target", "t")

    override fun check(data: Selector.Data): CompletableFuture<Void> {

        val range = data.read<Int>(0, "1")
        val blocked = data.read<Boolean>(1, "false")
        val point = data.read<Boolean>(2, "false")

        return createAwaitVoidFuture {
            val blocks = if (point) {
                listOf(
                    data.origin.getLivingEntity()
                        ?.getTargetBlock(if (blocked) setOf(Material.AIR) else Material.values().map { it }.toSet(), range) ?: return@createAwaitVoidFuture
                )
            } else {
                data.origin.getLivingEntity()?.getLineOfSight(if (blocked) setOf(Material.AIR) else Material.values().map { it }.toSet(), range)
                    ?: return@createAwaitVoidFuture
            }
            val entitys = mutableSetOf<Entity>()
            blocks.forEach {
                it.world.getNearbyEntities(it.location, 1.0, 1.0, 1.0).forEach { entity ->
                    entitys.add(entity)
                }
            }
            entitys.forEach {
                if (it is LivingEntity) {
                    data.container += it.toTarget()
                }
            }
        }
    }

}
