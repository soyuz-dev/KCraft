package org.soyuz.kcraft.computer

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import org.soyuz.kcraft.KCraftMenus

class ComputerMenu(
    containerId: Int,
    playerInventory: Inventory
) : AbstractContainerMenu(
    KCraftMenus.COMPUTER,
    containerId
) {

    var computer: ComputerBlockEntity? = null
        private set

    constructor(
        containerId: Int,
        playerInventory: Inventory,
        computer: ComputerBlockEntity
    ) : this(
        containerId,
        playerInventory
    ) {
        this.computer = computer
    }


    override fun quickMoveStack(
        player: Player,
        slotIndex: Int
    ): ItemStack =
        ItemStack.EMPTY

    override fun stillValid(
        player: Player
    ): Boolean =
        true

    override fun removed(player: Player) {
        super.removed(player)

        if (player is ServerPlayer) {
            computer?.removeViewer(player)
        }
    }
}