package org.soyuz.kcraft.computer

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import org.soyuz.kcraft.KCraftItems
import org.soyuz.kcraft.KCraftMenus

class ComputerMenu private constructor(
    containerId: Int,
    playerInventory: Inventory,
    golemContainer: Container
) : AbstractContainerMenu(
    KCraftMenus.COMPUTER,
    containerId
) {

    var computer: ComputerBlockEntity? = null
        private set


    // -------------------------------------------------------------------------
    // Client constructor
    // -------------------------------------------------------------------------

    constructor(
        containerId: Int,
        playerInventory: Inventory
    ) : this(
        containerId,
        playerInventory,
        SimpleContainer(1)
    )


    // -------------------------------------------------------------------------
    // Server constructor
    // -------------------------------------------------------------------------

    constructor(
        containerId: Int,
        playerInventory: Inventory,
        computer: ComputerBlockEntity
    ) : this(
        containerId,
        playerInventory,
        computer.golemSlot
    ) {
        this.computer = computer
    }


    // -------------------------------------------------------------------------
    // Slots
    // -------------------------------------------------------------------------

    init {
        // Slot 0: Ruby Golem
        addSlot(
            object : Slot(
                golemContainer,
                0,
                151,
                20
            ) {
                override fun mayPlace(
                    stack: ItemStack
                ): Boolean =
                    stack.`is`(
                        KCraftItems.RUBY_GOLEM
                    )
            }
        )

        // Player inventory: slots 1..27
        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(
                    Slot(
                        playerInventory,
                        column + row * 9 + 9,
                        8 + column * 18,
                        104 + row * 18
                    )
                )
            }
        }

        // Hotbar: slots 28..36
        for (column in 0 until 9) {
            addSlot(
                Slot(
                    playerInventory,
                    column,
                    8 + column * 18,
                    162
                )
            )
        }
    }


    // -------------------------------------------------------------------------
    // Menu behaviour
    // -------------------------------------------------------------------------

    override fun quickMoveStack(
        player: Player,
        slotIndex: Int
    ): ItemStack =
        ItemStack.EMPTY

    override fun stillValid(
        player: Player
    ): Boolean =
        true

    override fun removed(
        player: Player
    ) {
        super.removed(player)

        if (player is ServerPlayer) {
            computer?.removeViewer(player)
        }
    }
}