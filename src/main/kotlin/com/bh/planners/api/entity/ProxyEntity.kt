package com.bh.planners.api.entity

import org.bukkit.Location
import org.bukkit.World
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import java.util.UUID

interface ProxyEntity {

    val uniqueId: UUID

    val world: World

    val type : String

    val bukkitType : EntityType?

    val height: Double

    val vehicle: ProxyEntity?

    val name: String

    var customName: String?

    val location: Location

}