package com.bh.planners.core.module.mana

import com.bh.planners.api.*
import com.bh.planners.api.PlannersAPI.plannersProfile
import com.bh.planners.api.PlannersAPI.plannersProfileIsLoaded
import com.bh.planners.api.script.ScriptLoader
import com.bh.planners.core.effect.Target.Companion.target
import com.bh.planners.core.pojo.Context
import com.bh.planners.core.pojo.player.PlayerProfile
import com.bh.planners.util.runKetherThrow
import com.bh.planners.util.safeAsync
import eos.moe.dragoncore.network.PacketSender
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import taboolib.common.platform.function.submitAsync
import taboolib.common.platform.service.PlatformExecutor
import taboolib.common5.cdouble
import taboolib.module.kether.runKether

class DefaultManaManager : ManaManager {

    var maxmanaTask: PlatformExecutor.PlatformTask? = null

    var regainTask: PlatformExecutor.PlatformTask? = null

    override fun onEnable() {
        maxmanaTask = submitAsync(period = 100, comment = "MaxMana Update") {
            PlannersAPI.profiles.forEach { (_, profile) ->
                if (profile.player.isOnline) {
                    profile.updateFlag("@max-mana", calculate(profile))
                }
            }
        }
        regainTask = submitAsync(period = PlannersOption.regainManaPeriod, comment = "Mana Regain") {
            Bukkit.getOnlinePlayers().forEach { player ->
                val profile = player.plannersProfile
                val max = ManaManager.INSTANCE.getMaxMana(profile)
                val mana = ManaManager.INSTANCE.getMana(profile)
                if (max == mana) return@forEach
                nextRegainMana(player)
            }
        }
    }

    override fun onDisable() {
        maxmanaTask?.cancel()
        regainTask?.cancel()
    }

    private fun calculate(profile: PlayerProfile): Double {
        if (!profile.hasJob) {
            return 0.0
        }
        val context = object : Context.Impl0(profile.player.target()) {

            override var stackId: String = "Job:mana expression"

        }
        return runKetherThrow(context, 0.0) {
            ScriptLoader.createScript(context, profile.job!!.instance.option.manaCalculate).get().cdouble
        }!!
    }

    override fun getMaxMana(profile: PlayerProfile): Double {
        val max = profile.getFlag("@max-mana")?.toDouble() ?: calculate(profile)
        dragonSend(profile.player, maxMana = max)
        return max
    }


    override fun getMana(profile: PlayerProfile): Double {
        val max = getMaxMana(profile)
        val mana = profile.getFlag("@mana")?.toDouble() ?: 0.0
        return if (mana > max) {
            setMana(profile, max)
            profile.getFlag("@mana")?.toDouble() ?: 0.0
        } else {
            mana
        }
    }

    override fun addMana(profile: PlayerProfile, value: Double) {
        this.setMana(profile, this.getMana(profile) + value)
    }

    override fun takeMana(profile: PlayerProfile, value: Double) {
        this.setMana(profile, this.getMana(profile) - value)
    }

    override fun setMana(profile: PlayerProfile, value: Double) {
        val mana = value.coerceAtMost(getMaxMana(profile)).coerceAtLeast(0.0)
        dragonSend(profile.player, mana)
        profile.updateFlag("@mana", mana)
    }

    override fun getRegainMana(profile: PlayerProfile): Double {
        return regainManaValue(profile.player)
    }


    fun regainManaValue(player: Player): Double {
        val expression = getManaExpression(player) ?: return 0.0
        return runKether(0.0) {
            ScriptLoader.createScript(ContextAPI.create(player), expression).get().cdouble
        }!!
    }

    fun nextRegainMana(player: Player): Double {
        val value = regainManaValue(player)
        if (value <= 0.0) {
            return value
        }
        val profile = player.plannersProfile
        ManaManager.INSTANCE.addMana(profile, value)
        return value
    }

    fun getManaExpression(player: Player): String? {
        if (!player.plannersProfileIsLoaded) return null
        val profile = player.plannersProfile
        if (!profile.hasJob) return null
        val instance = profile.job!!.instance
        return instance.option.regainManaExperience ?: instance.router.regainManaExperience
        ?: PlannersOption.regainManaExperience
    }

    fun dragonSend(player: Player, mana: Double? = null, maxMana: Double? = null) {
        safeAsync {
            if (Bukkit.getPluginManager().isPluginEnabled("DragonCore")) {
                mana?.let { PacketSender.sendSyncPlaceholder(player, mapOf("planners_mana" to it.toString())) }
                maxMana?.let { PacketSender.sendSyncPlaceholder(player, mapOf("planners_maxMana" to it.toString())) }
            }
        }
    }

}