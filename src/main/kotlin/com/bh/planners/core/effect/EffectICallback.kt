package com.bh.planners.core.effect

import com.bh.planners.core.effect.inline.Incident.Companion.handleIncident
import com.bh.planners.core.effect.inline.IncidentEffectHit
import com.bh.planners.core.effect.inline.IncidentEffectTick
import com.bh.planners.core.effect.inline.IncidentHitEntity
import com.bh.planners.core.kether.game.ActionEffect
import com.bh.planners.core.pojo.Context
import com.bh.planners.core.pojo.Session
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

abstract class EffectICallback<T>(val name: String,val context: Context.SourceImpl) {

    val listeners = mutableMapOf<String, (T) -> Unit>()

    class Tick(name: String,context: Context.SourceImpl) : EffectICallback<MutableList<Location>>(name,context) {

        fun handle(location: Location) {
            this.handle(listOf(location))
        }

        fun handle(locations: List<Location>) {

            if (name == "__none__") return

            val mutableList = locations.toMutableList()

            listeners.forEach { it.value(mutableList) }

            val effectTick = IncidentEffectTick(mutableList)
            context.handleIncident(name,effectTick)
        }

    }

    class Hit(name: String,context: Context.SourceImpl) : EffectICallback<MutableList<LivingEntity>>(name,context) {

        fun handle(location: Location) {
            this.handle(listOf(location))
        }

        fun handle(locations: List<Location>) {

            if (name == "__none__") return

            locations.forEach {
                it.capture().thenAccept { entities ->
                    if (entities.isNotEmpty()) {
                        context.handleIncident(name, IncidentEffectHit(entities))
                    }
                }
            }

        }

    }

}