package com.bh.planners.core.selector.bukkit

import com.bh.planners.core.effect.Target
import com.bh.planners.core.effect.Target.Companion.toTarget
import com.bh.planners.core.selector.Selector
import taboolib.common5.Coerce
import java.util.concurrent.CompletableFuture

/**
 * 该操作会把实体目标转换为坐标目标
 * :@offset-r 1 1 1 0 0 基于原有地址偏移(1 1 1)xyz pitch yaw
 */
object OffsetRelative : Selector {

    override val names: Array<String>
        get() = arrayOf("offset-r", "offsetr", "offset-relative")

    override fun check(
        data: Selector.Data,
    ): CompletableFuture<Void> {
        val x = data.read<Double>(0, "0.0")
        val y = data.read<Double>(1, "0.0")
        val z = data.read<Double>(2, "0.0")
        val pitch = Coerce.toFloat(data.read<Double>(3, "0.0"))
        val yaw = Coerce.toFloat(data.read<Double>(4, "0.0"))

        val removes = mutableListOf<Target>()
        val addons = mutableListOf<Target>()

        data.container.forEach {
            if (it is Target.Entity) {
                removes += it

                val location = it.value.clone()
                location.add(x, y, z)
                location.yaw += yaw
                location.pitch += pitch
                addons += location.toTarget()

            } else if (it is Target.Location) {
                it.value.add(x, y, z)
            }
        }

        data.container.removeIf { it in removes }
        data.container.addAll(addons)

        return CompletableFuture.completedFuture(null)
    }
}