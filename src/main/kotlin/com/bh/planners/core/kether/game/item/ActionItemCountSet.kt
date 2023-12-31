package com.bh.planners.core.kether.game.item

import com.bh.planners.core.kether.bukkitPlayer
import com.bh.planners.core.kether.execLivingEntity
import com.bh.planners.core.kether.game.item.ItemOperator.getNumber
import com.bh.planners.core.kether.read
import org.bukkit.inventory.ItemStack
import taboolib.library.kether.ParsedAction
import taboolib.module.kether.ScriptAction
import taboolib.module.kether.ScriptFrame
import taboolib.platform.util.isAir
import taboolib.platform.util.modifyLore
import taboolib.type.BukkitEquipment
import java.util.concurrent.CompletableFuture

class ActionItemCountSet(
    val slot: ParsedAction<*>,
    val keyword: ParsedAction<*>,
    val amount: ParsedAction<*>,
    val selector: ParsedAction<*>?,
) : ScriptAction<Void>() {


    override fun run(frame: ScriptFrame): CompletableFuture<Void> {

        frame.read<BukkitEquipment>(slot).thenAccept { slot ->
            frame.read<String>(keyword).thenAccept { keyword ->
                frame.read<Int>(amount).thenAccept { amount ->
                    if (selector != null) {
                        frame.execLivingEntity(selector) {
                            execute(slot.getItem(this), keyword, amount)
                        }
                    } else {
                        execute(slot.getItem(frame.bukkitPlayer()), keyword, amount)
                    }
                }
            }
        }

        return CompletableFuture.completedFuture(null)
    }

    fun execute(item: ItemStack?, keyword: String, amount: Int) {
        if (item == null || item.isAir() || item.itemMeta?.hasLore() == false) return
        item.modifyLore {
            this.forEachIndexed { index, s ->
                if (s.contains(keyword)) {
                    this[index] = s.replace(getNumber(s), amount.toString())
                }
            }
        }
    }

}