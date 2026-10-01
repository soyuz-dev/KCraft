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

    companion object {

        /*
         * ---------------------------------------------------------------------
         * GUI layout
         * ---------------------------------------------------------------------
         *
         * ComputerScreen uses the same constants when drawing the visual
         * backgrounds, so the interactive slots and their graphics cannot
         * accidentally drift apart.
         */

        const val GUI_WIDTH =
            400

        const val GUI_HEIGHT =
            190


        // Ruby Golem slot ------------------------------------------------------

        const val GOLEM_SLOT_X =
            370

        const val GOLEM_SLOT_Y =
            20


        // Player inventory ----------------------------------------------------

        const val INVENTORY_X =
            226

        const val INVENTORY_Y =
            84

        const val HOTBAR_Y =
            142
    }


    /*
     * Only the server-side menu has the actual ComputerBlockEntity.
     *
     * The client receives a dummy container and relies on Minecraft's normal
     * menu synchronisation for slot contents.
     */
    var computer: ComputerBlockEntity? =
        null
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
        this.computer =
            computer
    }


    // -------------------------------------------------------------------------
    // Slots
    // -------------------------------------------------------------------------

    init {

        /*
         * Slot 0:
         *
         * The computer's dedicated Ruby Golem slot.
         *
         * This is backed by:
         *
         *   - ComputerBlockEntity.golemSlot on the server
         *   - a temporary SimpleContainer on the client
         *
         * Minecraft synchronises the two through the normal menu system.
         */
        addSlot(
            object : Slot(
                golemContainer,
                0,
                GOLEM_SLOT_X,
                GOLEM_SLOT_Y
            ) {

                /*
                 * Nothing except an actual Ruby Golem item may be placed
                 * in this slot.
                 */
                override fun mayPlace(
                    stack: ItemStack
                ): Boolean =
                    stack.`is`(
                        KCraftItems.RUBY_GOLEM
                    )
            }
        )


        /*
         * Player main inventory.
         *
         * Menu slot indices:
         *
         *   0       Ruby Golem
         *   1..27   Player inventory
         *   28..36  Hotbar
         */
        for (row in 0 until 3) {
            for (column in 0 until 9) {
                addSlot(
                    Slot(
                        playerInventory,
                        column +
                                row * 9 +
                                9,

                        INVENTORY_X +
                                column * 18,

                        INVENTORY_Y +
                                row * 18
                    )
                )
            }
        }


        /*
         * Player hotbar.
         */
        for (column in 0 until 9) {
            addSlot(
                Slot(
                    playerInventory,
                    column,

                    INVENTORY_X +
                            column * 18,

                    HOTBAR_Y
                )
            )
        }
    }


    // -------------------------------------------------------------------------
    // Menu behaviour
    // -------------------------------------------------------------------------

    /*
     * Shift-click support can come later.
     *
     * Returning EMPTY simply means quick-moving items is currently disabled.
     */
    override fun quickMoveStack(
        player: Player,
        slotIndex: Int
    ): ItemStack =
        ItemStack.EMPTY


    /*
     * TODO:
     *
     * Eventually this should probably verify that the player is still close
     * enough to the computer.
     *
     * For now the menu remains valid while open.
     */
    override fun stillValid(
        player: Player
    ): Boolean =
        true


    /*
     * Stop tracking the player as a computer viewer when the menu closes.
     */
    override fun removed(
        player: Player
    ) {
        super.removed(
            player
        )

        if (player is ServerPlayer) {
            computer?.removeViewer(
                player
            )
        }
    }
}