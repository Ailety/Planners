package com.bh.planners.api

import com.bh.planners.api.Assembly.upgradePoints
import com.bh.planners.api.script.ScriptLoader
import com.bh.planners.core.pojo.Context
import com.bh.planners.core.pojo.Router
import com.bh.planners.core.pojo.Skill
import com.bh.planners.core.pojo.player.PlayerJob
import org.bukkit.entity.Player
import taboolib.common5.Coerce
import taboolib.module.kether.runKether
import java.util.concurrent.CompletableFuture

val VARIABLE_0 = Skill.Variable("__null__", "0")

val VARIABLE_1 = Skill.Variable("__null__", "1")

val VARIABLE_2 = Skill.Variable("__null__", "2")

val VARIABLE_TRUE = Skill.Variable("__null__", "true")

val VARIABLE_FALSE = Skill.Variable("__null__", "true")

val VARIABLE_NULL = Skill.Variable("__null__", "null")

val PlayerJob.optUpgradePoints: String?
    get() = instance.upgradePoints ?: rouUpgradePoints ?: PlannersOption.upgradePoints

val PlayerJob.router: Router
    get() = instance.router

val PlayerJob.rouUpgradePoints: String?
    get() = router.upgradePoints

val PlayerJob.Skill.levelCap: Int
    get() = instance.option.levelCap

val PlayerJob.Skill.maxLevel: Int
    get() = levelCap

val PlayerJob.Skill.optVariables: List<Skill.Variable>
    get() = instance.option.variables

val PlayerJob.Skill.optAsync: Boolean
    get() = instance.option.async

val PlayerJob.Skill.needPointsVariable: Skill.Variable
    get() = instance.needPointsVariable

val Skill.needPointsVariable: Skill.Variable
    get() = getVariable("upgrade-points", VARIABLE_0)

fun PlayerJob.Skill.getNeedPoints(player: Player): CompletableFuture<Int> {
    return ScriptLoader
        .createScript(ContextAPI.create(player, this), needPointsVariable.expression) {
            it["@level"] = level
        }.thenApply { Coerce.toInteger(it) }
}

fun Skill.getNeedPoints(player: Player, level: Int): CompletableFuture<Int>? {
    return ScriptLoader.createScript(ContextAPI.create(player, this, level)!!, needPointsVariable.expression) {
        it["@level"] = level
    }.thenApply { Coerce.toInteger(it) }
}

fun Skill.getVariable(id: String, def: Skill.Variable): Skill.Variable {
    return option.variables.firstOrNull { it.key == id } ?: def
}

fun PlayerJob.Skill.runVariable(player: Player, id: String): CompletableFuture<Any?> {
    return instance.runVariable(player, level, id)
}

fun PlayerJob.Skill.runVariable(player: Player, variable: Skill.Variable): CompletableFuture<Any?> {
    return instance.runVariable(player, level, variable)
}

fun Skill.runVariable(player: Player, level: Int, id: String): CompletableFuture<Any?> {
    val context = ContextAPI.create(player, this, level) ?: return CompletableFuture.completedFuture(null)
    return runVariable(context, id)
}

fun Skill.runVariable(player: Player, level: Int, variable: Skill.Variable): CompletableFuture<Any?> {
    val context = ContextAPI.create(player, this, level) ?: return CompletableFuture.completedFuture(null)
    return runVariable(context, variable)
}

fun Skill.runVariable(context: Context, id: String): CompletableFuture<Any?> {
    val variable = getVariable(id, VARIABLE_NULL)
    if (variable.expression == "null") {
        return CompletableFuture.completedFuture(null)
    }
    return runVariable(context, variable)
}

fun runVariable(context: Context, variable: Skill.Variable): CompletableFuture<Any?> {
    return runKether {
        ScriptLoader.createScript(context, variable.expression)
    } ?: CompletableFuture.completedFuture(null)
}