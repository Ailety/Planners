package com.bh.planners.core.kether.game

import com.bh.planners.core.kether.*
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

class ActionFireTicks(val ticks: ParsedAction<*>, val selector: ParsedAction<*>?) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {

        frame.readAccept<Int>(ticks) { ticks ->
            if (selector != null) {
                frame.execEntity(selector) { fireTicks = ticks }
            } else {
                frame.bukkitPlayer()?.fireTicks = ticks
            }
        }
        return CompletableFuture.completedFuture(null)
    }

    companion object {

        /**
         * 使目标点燃
         * fireTicks [ticks] [selector]
         */
        @KetherParser(["fireTicks"], namespace = NAMESPACE, shared = true)
        fun parser() = scriptParser {
            val power = it.nextParsedAction()
            ActionFireTicks(power, it.nextSelectorOrNull())
        }
    }
}