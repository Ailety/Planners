package com.bh.planners.api.compat

import com.bh.planners.api.ContextAPI
import com.bh.planners.api.PlannersOption
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.DefaultFlag
import org.bukkit.Bukkit
import org.bukkit.entity.Player

object WorldGuardHook {

    private val worldGuard = Bukkit.getPluginManager().isPluginEnabled("WorldGuard")

    fun cast(player: Player) : Boolean {
        if (!worldGuard) return true
        if (PlannersOption.root.getBoolean("WorldGuard.enable")) {
            val localPlayer = WorldGuardPlugin().wrapPlayer(player)
            val query = WorldGuardPlugin().regionContainer.createQuery()
            val skill = PlannersOption.root.getString("WorldGuard.castSkill") ?: ""
            if (!query.testState(player.location, localPlayer, DefaultFlag.PVP)) {
                if (PlannersOption.root.getBoolean("WorldGuard.skill")) {
                    ContextAPI.create(player, skill, 1)?.cast()
                }
                return false
            }
        }
        return true
    }



}