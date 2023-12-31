package com.bh.planners.core.kether.compat.adyeshach


import com.bh.planners.core.kether.compat.adyeshach.ActionAdyeshach.execAdyeshachEntity
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import java.util.concurrent.CompletableFuture


class ActionAdyeshachRemove(val selector: ParsedAction<*>) : ScriptAction<Void>() {
    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        frame.execAdyeshachEntity(selector) {
            this.delete()
        }
        return CompletableFuture.completedFuture(null)
    }
}
