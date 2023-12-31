package com.bh.planners.api

import com.bh.planners.api.event.PluginReloadEvent
import com.bh.planners.api.script.ScriptLoader
import com.bh.planners.core.module.mana.ManaManager
import com.bh.planners.core.pojo.Job
import com.bh.planners.core.pojo.Router
import com.bh.planners.core.pojo.Skill
import com.bh.planners.util.files
import taboolib.common.LifeCycle
import taboolib.common.platform.Awake
import taboolib.common.platform.event.SubscribeEvent
import taboolib.module.configuration.Config
import taboolib.module.configuration.Configuration
import java.io.File

object PlannersLoader {

    @Config("router.yml")
    lateinit var routerConfig: Configuration

    @Awake(LifeCycle.ENABLE)
    fun loadJobs() {
        PlannersAPI.jobs.clear()
        files("job", listOf("job_def0.yml", "job_def1.yml")) {
            PlannersAPI.jobs += Job(it.toYamlName(), Configuration.loadFromFile(it))
        }
    }

    fun File.toYamlName(): String {
        return name.replace(".yml", "")
    }

    @Awake(LifeCycle.ENABLE)
    fun loadSkills() {
        PlannersAPI.skills.clear()
        files("skill", listOf("skill_def0.yml", "skill_def1.yml", "禁止释放.yml")) {
            PlannersAPI.skills += Skill(it.toYamlName(), Configuration.loadFromFile(it))
        }
        ScriptLoader.autoLoad()
    }

    @Awake(LifeCycle.ENABLE)
    fun loadGroups() {
        PlannersAPI.routers.clear()
        routerConfig.reload()
        routerConfig.getKeys(false).forEach {
            PlannersAPI.routers += Router(routerConfig.getConfigurationSection(it)!!)
        }
    }

    @Awake(LifeCycle.ENABLE)
    fun loadMana() {
        ManaManager.INSTANCE.onEnable()
    }

    @Awake(LifeCycle.DISABLE)
    fun unloadMana() {
        ManaManager.INSTANCE.onDisable()
    }


    @SubscribeEvent
    fun e(e: PluginReloadEvent) {
        routerConfig.reload()
        this.loadGroups()
        this.loadJobs()
        this.loadSkills()
    }

}
