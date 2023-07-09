package com.bh.planners.api

import com.bh.planners.api.PlannersAPI.plannersProfile
import org.bukkit.event.entity.PlayerDeathEvent
import taboolib.common.platform.event.SubscribeEvent

object PlayerState {

    val stopSkill = PlannersOption.root.getBoolean("skill-stop", true)

    @SubscribeEvent
    fun e(e: PlayerDeathEvent) {
        if (stopSkill) {
            e.entity.plannersProfile.runningScripts.map { it.value.let { script -> script.service.terminateQuest(script) } }
        }
    }

}