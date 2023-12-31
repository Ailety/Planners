package com.bh.planners.api.compat

import com.bh.planners.api.PlannersAPI.plannersProfile
import com.bh.planners.api.addExperience
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent
import io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent
import org.bukkit.entity.Player
import taboolib.common.platform.event.OptionalEvent
import taboolib.common.platform.event.SubscribeEvent
import taboolib.common.util.random
import taboolib.common5.eqic

object MythicMobsHook {

    @SubscribeEvent(bind = "io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMobDeathEvent")
    fun e(ope: OptionalEvent) {
        val e = ope.get<MythicMobDeathEvent>()
        val killer = e.killer as? Player ?: return
        val config = e.mobType.config
        val string = config.getString("conch-exp", null) ?: return
        val split = string.split("-").toTypedArray()
        val start = split[0].toInt()
        val end = if (split.size == 2) split[1].toInt() else start
        val random = random(start, end)
        killer.plannersProfile.addExperience(random)
    }

    @SubscribeEvent(bind = "io.lumine.xikage.mythicmobs.api.bukkit.events.MythicMechanicLoadEvent")
    fun e1(e: OptionalEvent) {
        val event = e.get<MythicMechanicLoadEvent>()
        if (event.mechanicName.eqic("pl-skill") || event.mechanicName.eqic("planners-skill")) {
            event.register(MythicMobSkill(event.container.configLine, event.config))
        }
    }

}