package com.bh.planners.core.selector.bukkit

import com.bh.planners.core.effect.Target.Companion.getLocation
import com.bh.planners.core.effect.Target.Companion.toTarget
import com.bh.planners.core.effect.isPointInEntitySector
import com.bh.planners.core.selector.Selector
import org.bukkit.entity.LivingEntity
import taboolib.common.platform.function.submit
import java.util.concurrent.CompletableFuture
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * 选中根据原点来定义的范围实体
 * @range 10
 * @range 5 5 5
 */
object Range : Selector {

    override val names: Array<String>
        get() = arrayOf("range", "r")

    override fun check(data: Selector.Data): CompletableFuture<Void> {
        val location = data.origin.getLocation() ?: return CompletableFuture.completedFuture(null)

        val x = data.read<Double>(0, "0.0")
        val y = data.read<Double>(1, x.toString())
        val z = data.read<Double>(2, x.toString())

        val future = CompletableFuture<Void>()
        submit(async = false) {
            if (x == y && y == z) {
                location.world?.getNearbyEntities(location, x+5, x+5, x+5)?.forEach {
                    if (isPointInEntitySector(it.location, location, x + sqrt(it.width.pow(2.0) * 2), 360.0)) {
                        if (it is LivingEntity) {
                            data.container += it.toTarget()
                        }
                    }
                }
            } else {
                location.world?.getNearbyEntities(location, x, y, z)?.forEach {
                    if (it is LivingEntity) {
                        data.container += it.toTarget()
                    }
                }
            }
            future.complete(null)
        }
        return future
    }
}
