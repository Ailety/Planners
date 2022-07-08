package com.bh.planners.core.kether.game

import com.bh.planners.core.kether.NAMESPACE
import com.bh.planners.core.kether.exec
import com.bh.planners.core.kether.selectorAction
import org.bukkit.Location
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.KetherParser
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.module.kether.scriptParser
import java.util.concurrent.CompletableFuture

class ActionExplosion(
    val power: ParsedAction<*>,
    val selector: ParsedAction<*>?
) : ScriptAction<Void>() {

    override fun run(frame: ScriptFrame): CompletableFuture<Void> {
        frame.newFrame(power).run<Any>().thenApply {
            frame.exec(selector!!) {
                val loc = when (this) {
                    is com.bh.planners.core.skill.effect.Target.Entity -> this.entity.location
                    is com.bh.planners.core.skill.effect.Target.Location -> this.value
                    else -> return@exec
                }
                createExplosion(loc, Coerce.toFloat(it))
            }
        }
        return CompletableFuture.completedFuture(null)
    }

    private fun createExplosion(loc: Location, power: Float) {
        loc.world!!.createExplosion(loc.x, loc.y, loc.z, power, false, false)
    }

    companion object {

        /**
         * 在指定(目标)坐标处召唤一次爆炸
         * explosion [power] [selector]
         */
        @KetherParser(["explosion"], namespace = NAMESPACE)
        fun parser() = scriptParser {
            val power = it.next(ArgTypes.ACTION)
            ActionExplosion(power, it.selectorAction())
        }
    }
}