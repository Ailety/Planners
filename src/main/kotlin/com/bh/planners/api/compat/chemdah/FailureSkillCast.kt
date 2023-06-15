package com.bh.planners.api.compat.chemdah

import com.bh.planners.api.PlannersAPI.plannersProfile
import com.bh.planners.api.event.PlayerCastSkillEvents
import com.bh.planners.util.isWorld
import ink.ptms.chemdah.core.quest.objective.ObjectiveCountableI

object FailureSkillCast:ObjectiveCountableI<PlayerCastSkillEvents.Failure>() {
    override val event: Class<PlayerCastSkillEvents.Failure> = PlayerCastSkillEvents.Failure::class.java
    override val name: String = "failure skill cast"

    init {
        handler{
            it.player
        }
        addSimpleCondition("skill") { data, e ->
            data.toString() == e.skill.key
        }
        addSimpleCondition("level") { data, e ->
            e.player.plannersProfile.job!!.level >= data.toInt()
        }
        PreSkillCast.addSimpleCondition("world") { data, e ->
            e.player.world.isWorld(data.toString())
        }
       addSimpleCondition("job") { data, event ->
            event.player.plannersProfile.job?.name == data.toString()
        }
    }
}