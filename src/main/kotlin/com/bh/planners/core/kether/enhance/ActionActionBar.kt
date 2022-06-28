package com.bh.planners.core.kether.enhance

import com.bh.planners.core.kether.*
import org.bukkit.entity.Player
import taboolib.common.platform.ProxyPlayer
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import taboolib.platform.util.sendActionBar
import java.util.concurrent.CompletableFuture

/**
 * @author IzzelAliz
 */
class ActionActionBar(val message: ParsedAction<*>, val selector: ParsedAction<*>?) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        return frame.newFrame(message).run<Any>().thenAccept { message ->

            if (selector != null) {
                frame.newFrame(selector).run<Any>().thenAccept {
                    frame.createTargets(selector).thenAccept {
                        it.forEachPlayer {
                            sendActionBar(message.toString().trimIndent().replace("@sender", this.name))
                        }
                    }
                }
            } else {
                val asPlayer = frame.asPlayer() ?: return@thenAccept
                asPlayer.sendActionBar(message.toString().trimIndent().replace("@sender", asPlayer.name))
            }
        }
    }


    internal object Parser {

        @KetherParser(["actionbar"], namespace = NAMESPACE)
        fun parser() = scriptParser {
            ActionActionBar(it.next(ArgTypes.ACTION), it.selectorAction())
        }
    }
}