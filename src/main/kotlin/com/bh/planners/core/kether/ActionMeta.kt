package com.bh.planners.core.kether

import com.bh.planners.api.ManaCounter.toCurrentMana
import com.bh.planners.api.ManaCounter.toMaxMana
import com.bh.planners.api.Counting
import com.bh.planners.api.maxLevel
import com.bh.planners.api.optAsync
import com.bh.planners.api.optVariables
import com.bh.planners.core.effect.Target.Companion.getLocation
import com.bh.planners.core.effect.Target.Companion.toTarget
import com.bh.planners.core.kether.meta.ActionMetaOrigin
import taboolib.library.kether.ArgTypes
import taboolib.module.kether.*

class ActionMeta {


    companion object {

        /**
         * meta executor name
         * meta executor uuid
         * meta executor loc
         * meta executor mana
         * meta executor mana max
         *
         * meta skill name
         * meta skill async
         * meta skill level
         * meta skill level cap
         *
         * meta origin
         */
        @KetherParser(["meta"], namespace = NAMESPACE, shared = true)
        fun parser() = scriptParser {
            it.switch {

                case("skill") {
                    when (expects("id", "name", "async", "level", "level-cap", "level-max", "shortcut", "countdown")) {
                        "id" -> actionSkillNow { it.key }
                        "name" -> actionSkillNow { it.name }
                        "async" -> actionSkillNow { it.optAsync }
                        "level" -> actionSkillNow { it.level }
                        "level-cap", "level-max", "maxlevel", "max-level" -> actionSkillNow { it.maxLevel }
                        "shortcut" -> actionSkillNow { it.keySlot?.name ?: "暂无" }
                        "countdown" -> actionSkillNow { Counting.getCountdown(bukkitPlayer()!!, it.instance) }
                        else -> actionNow { "error" }
                    }

                }

                case("executor") {
                    when (expects("name", "uuid", "loc", "location", "mana", "max-mana")) {
                        "name" -> actionNow { executor().name }
                        "uuid" -> actionNow { bukkitPlayer()!!.uniqueId.toString() }
                        "loc", "location" -> actionNow { bukkitPlayer()!!.location.clone() }
                        "mana" -> actionProfileNow { it.toCurrentMana() }
                        "max-mana", "maxmana" -> actionProfileNow { it.toMaxMana() }
                        else -> actionNow { "error" }
                    }
                }
                case("origin") {
                    try {
                        mark()
                        expects("to", "set", "=","bind")
                        try {
                            it.mark()
                            expects("they","the")
                            ActionMetaOrigin.Set(it.nextParsedAction())
                        }catch (_:Throwable) {
                            it.reset()
                            ActionMetaOrigin.Set(it.nextParsedAction())
                        }

                    } catch (e: Throwable) {
                        reset()
                        actionNow { origin() }
                    }
                }
            }
        }

    }

}