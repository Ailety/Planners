package com.bh.planners.core.kether.effect.selector

import com.bh.planners.core.kether.effect.Target
import com.bh.planners.core.kether.effect.Target.Companion.toTarget
import org.bukkit.entity.Player

object Self : Selector {

    override val names: Array<String>
        get() = arrayOf("self", "this")

    override fun check(args: String, sender: Player, container: Target.Container) {
        container.add(sender.toTarget())
    }

}
