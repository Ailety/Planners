package com.bh.planners.api.event

import com.bh.planners.core.pojo.Skill
import org.bukkit.entity.Player
import taboolib.platform.type.BukkitProxyEvent

class PlayerCastSkillEvents {

    class Pre(val player: Player, val skill: Skill) : BukkitProxyEvent()

    class Record(val player: Player, val skill: Skill) : BukkitProxyEvent()

    class Post(val player: Player, val skill: Skill) : BukkitProxyEvent()

}