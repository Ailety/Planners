package com.bh.planners.core.timer

import taboolib.library.configuration.ConfigurationSection


class Template(val id: String, var root: ConfigurationSection) {

    val triggers = root.getStringList("__option__.triggers")

    val async = root.getBoolean("__option__.async")

    val action = root.getString("action")


}
