package com.bh.planners.command

import com.bh.planners.api.PlannersAPI.plannersProfile
import com.bh.planners.api.hasJob
import com.bh.planners.core.kether.bukkitPlayer
import com.bh.planners.core.module.mana.ManaManager
import org.bukkit.Bukkit
import taboolib.common.platform.ProxyCommandSender
import taboolib.common.platform.command.*
import taboolib.common5.Coerce
import taboolib.common5.cdouble
import taboolib.expansion.createHelper
import taboolib.platform.util.sendLang

@CommandHeader("plannersmana")
object PlannersManaCommand {


    @CommandBody
    val main = mainCommand {
        createHelper()
    }

    @CommandBody
    val give = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender> { sender, context -> Bukkit.getOnlinePlayers().map { it.name } }

            dynamic("value") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val player = context.player("player").bukkitPlayer()!!
                    if (player.hasJob) {
                        ManaManager.INSTANCE.addMana(player.plannersProfile,argument.cdouble)
                        player.sendLang("player-get-mana", argument)
                    }
                }
            }
        }
    }

    @CommandBody
    val take = subCommand {
        dynamic("player") {
            suggestion<ProxyCommandSender> { sender, context -> Bukkit.getOnlinePlayers().map { it.name } }

            dynamic("value") {
                execute<ProxyCommandSender> { sender, context, argument ->
                    val player = context.player("player").bukkitPlayer()!!
                    if (player.hasJob) {
                        ManaManager.INSTANCE.takeMana(player.plannersProfile,argument.cdouble)
                        player.sendLang("player-take-mana", argument)
                    }
                }
            }
        }
    }


}