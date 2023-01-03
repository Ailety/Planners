package com.bh.planners.core.kether.compat.buffsystem

import com.bh.planners.core.kether.*
import com.google.gson.Gson
import com.skillw.buffsystem.BuffSystem.buffManager
import com.skillw.buffsystem.api.BuffAPI.clearBuff
import com.skillw.buffsystem.api.BuffAPI.giveBuff
import com.skillw.buffsystem.api.BuffAPI.removeBuff
import org.bukkit.Bukkit
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import taboolib.common.platform.function.submit
import taboolib.common5.Coerce
import taboolib.library.kether.ArgTypes
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.*
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * BuffSystem
 *
 * @author Glom_
 * @since 2022年8月14日 22:02:16
 */
class ActionBuff {

    class Give(
        val source: ParsedAction<*>,
        val buffKey: ParsedAction<*>,
        val dataJson: ParsedAction<*>,
        val selector: ParsedAction<*>?
    ) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            frame.runTransfer0<String>(source) { source ->
                frame.runTransfer0<String>(buffKey) { buffKey ->
                    submit {
                        frame.runTransfer0<String>(dataJson) { dataJson ->
                            val buff = buffManager[buffKey] ?: error("buff $buffKey not found")
                            if (selector != null) {
                                frame.execLivingEntity(selector) { giveBuff(source,buff,"{$dataJson}") }
                            } else {
                                val viewer = frame.script().sender?.castSafely<Player>() ?: error("No player selected.")
                                viewer.giveBuff(source,buff,"{$dataJson}")
                            }
                        }
                    }
                }
            }
            return CompletableFuture.completedFuture(null)
        }
    }

    class Remove(val source: ParsedAction<*>, val selector: ParsedAction<*>?) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            frame.newFrame(source).run<String>().thenApplyAsync({ source ->
                if (selector != null) {
                    frame.execLivingEntity(selector) { removeBuff(source) }
                } else {
                    val viewer = frame.script().sender?.castSafely<Player>() ?: error("No player selected.")
                    viewer.removeBuff(source)
                }

            }, frame.context().executor)
            return CompletableFuture.completedFuture(null)
        }
    }

    class Clear(val selector: ParsedAction<*>?) : ScriptAction<Void>() {

        override fun run(frame: ScriptFrame): CompletableFuture<Void> {
            submit {
                if (selector != null) {
                    frame.execLivingEntity(selector) { clearBuff()}
                } else {
                    val viewer = frame.script().sender?.castSafely<Player>() ?: error("No player selected.")
                    viewer.clearBuff()
                }
            }
            return CompletableFuture.completedFuture(null)
        }
    }

    companion object {

        /**
         * buff give "source" "testbuff" inline "duration:{{ time }},param1:{{ param1 }}" <they selector>  给buff，倒数第二个参数是json式的传参
         * buff remove "source" <they selector> 删除实体上的buff
         * buff removeIf "param1:{{ param1 }}" <they selector> 删除实体上符合条件的buff
         * buff clear <they selector> 清除实体上的buff
         */
        @KetherParser(["buff"], namespace = NAMESPACE, shared = true)
        fun parser() = scriptParser {
            it.switch {
                case("give") {
                    Give(
                        it.nextParsedAction(),
                        it.nextParsedAction(),
                        it.nextParsedAction(),
                        it.selectorAction()
                    )
                }
                case("clear") { Clear(it.selectorAction()) }
                case("remove") { Remove(it.nextParsedAction(), it.selectorAction()) }
            }
        }
    }
}