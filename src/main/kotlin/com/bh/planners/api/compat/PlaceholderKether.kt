package com.bh.planners.api.compat

import com.bh.planners.api.ContextAPI
import com.bh.planners.core.kether.namespaces
import com.bh.planners.core.pojo.Context
import org.bukkit.entity.Player
import taboolib.common.platform.function.adaptPlayer
import taboolib.module.kether.KetherShell
import taboolib.module.kether.printKetherErrorMessage
import taboolib.platform.compat.PlaceholderExpansion
import java.util.*

object PlaceholderKether : PlaceholderExpansion {

    private val cache = Collections.synchronizedMap(mutableMapOf<Player, Context.Impl0>())

    private fun getContext(player: Player) = ContextAPI.create(player)

    override val identifier: String
        get() = "planners"

    override fun onPlaceholderRequest(player: Player?, args: String): String {
        return try {
            KetherShell.eval(args, sender = adaptPlayer(player!!), namespace = namespaces) {
                rootFrame().variables()["@Context"] = getContext(player)
            }.get().toString()
        } catch (e: Throwable) {
            e.printKetherErrorMessage()
            "none-error"
        }
    }

}
