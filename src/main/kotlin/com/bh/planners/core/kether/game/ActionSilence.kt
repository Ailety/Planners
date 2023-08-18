package com.bh.planners.core.kether.game

import com.bh.planners.api.event.PlayerCastSkillEvents
import com.bh.planners.api.event.PlayerSilenceEvent
import com.bh.planners.core.kether.common.CombinationKetherParser
import com.bh.planners.core.kether.common.KetherHelper.containerOrSender
import com.bh.planners.core.kether.common.SimpleKetherParser
import taboolib.common.platform.event.EventPriority
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.kether.*
import java.util.*
import kotlin.collections.set

@CombinationKetherParser.Used
object ActionSilence : SimpleKetherParser("silence") {

    override fun run(): ScriptActionParser<out Any?> {
        return combinationParser {
            it.group(command("event", "callevent", "call", then = bool()), long(), containerOrSender()).apply(it) { event, tick, container ->
                now {
                    container.forEachPlayer {
                        silenceMap[this.uniqueId] = tick
                        if (event) { PlayerSilenceEvent(this,tick).call()}
                    }
                }
            }
        }
    }

    private val silenceMap = mutableMapOf<UUID, Long>()

    @SubscribeEvent(EventPriority.LOWEST)
    fun e(e: PlayerCastSkillEvents.Pre) {
        val time = silenceMap[e.player.uniqueId] ?: return
        if (System.currentTimeMillis() >= time) {
            silenceMap.remove(e.player.uniqueId)
        } else {
            e.isCancelled = true
        }
    }
}