package org.soyuz.kcraft.client.computer

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.client.gui.GuiGraphicsExtractor
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen
import net.minecraft.client.input.CharacterEvent
import net.minecraft.client.input.KeyEvent
import net.minecraft.network.chat.Component
import net.minecraft.util.ARGB
import net.minecraft.world.entity.player.Inventory
import org.lwjgl.glfw.GLFW
import org.soyuz.kcraft.computer.ComputerMenu
import org.soyuz.kcraft.computer.ComputerMode
import org.soyuz.kcraft.computer.Terminal
import org.soyuz.kcraft.network.c2s.ComputerInputPayload
import org.soyuz.kcraft.network.c2s.RequestComputerDisplayStatePayload


class ComputerScreen(
    menu: ComputerMenu,
    inventory: Inventory,
    title: Component
) : AbstractContainerScreen<ComputerMenu>(
    menu,
    inventory,
    title,

    /*
     * KCraft is much wider than a normal Minecraft container.
     *
     * The terminal occupies the left half while the player's inventory and
     * Ruby Golem controls occupy the right half.
     */
    ComputerMenu.GUI_WIDTH,
    ComputerMenu.GUI_HEIGHT
) {

    init {

        /*
         * Ask the server for the terminal's current display when the GUI
         * first opens.
         *
         * Inventory contents do not need a custom packet; Minecraft's
         * container synchronisation already handles them.
         */
        ClientPlayNetworking.send(
            RequestComputerDisplayStatePayload
        )
    }


    // -------------------------------------------------------------------------
    // Terminal state
    // -------------------------------------------------------------------------

    /*
     * Client-side snapshot of what the server-side terminal currently shows.
     */
    private var visibleLines =
        List(
            ComputerMode.VISIBLE_LINES
        ) {
            ""
        }

    private var cursorRow =
        0

    private var cursorColumn =
        0


    /**
     * Called when the server sends us a new terminal display state.
     */
    fun updateTerminalState(
        lines: List<String>,
        cursorRow: Int,
        cursorColumn: Int
    ) {
        this.visibleLines =
            lines

        this.cursorRow =
            cursorRow

        this.cursorColumn =
            cursorColumn
    }


    // -------------------------------------------------------------------------
    // Keyboard input
    // -------------------------------------------------------------------------

    /*
     * Ordinary typed characters go to the KCraft terminal rather than being
     * interpreted locally.
     */
    override fun charTyped(
        event: CharacterEvent
    ): Boolean {
        ClientPlayNetworking.send(
            ComputerInputPayload(
                type =
                    ComputerInputPayload.Type.CHARACTER,

                character =
                    event.codepoint
            )
        )

        return true
    }


    /*
     * Translate useful keyboard controls into KCraft terminal input.
     */
    override fun keyPressed(
        event: KeyEvent
    ): Boolean {

        val input =
            when {

                event.key ==
                        GLFW.GLFW_KEY_ENTER ->

                    ComputerInputPayload.Type.ENTER


                event.key ==
                        GLFW.GLFW_KEY_BACKSPACE ->

                    ComputerInputPayload.Type.BACKSPACE


                event.key ==
                        GLFW.GLFW_KEY_UP ->

                    ComputerInputPayload.Type.UP


                event.key ==
                        GLFW.GLFW_KEY_DOWN ->

                    ComputerInputPayload.Type.DOWN


                event.key ==
                        GLFW.GLFW_KEY_LEFT ->

                    ComputerInputPayload.Type.LEFT


                event.key ==
                        GLFW.GLFW_KEY_RIGHT ->

                    ComputerInputPayload.Type.RIGHT


                event.key ==
                        GLFW.GLFW_KEY_S &&
                        event.modifiers and
                        GLFW.GLFW_MOD_CONTROL != 0 ->

                    ComputerInputPayload.Type.SAVE


                event.key ==
                        GLFW.GLFW_KEY_X &&
                        event.modifiers and
                        GLFW.GLFW_MOD_CONTROL != 0 ->

                    ComputerInputPayload.Type.EXIT


                else ->
                    null
            }


        /*
         * If KCraft recognised the key, consume it.
         */
        if (input != null) {
            ClientPlayNetworking.send(
                ComputerInputPayload(
                    input
                )
            )

            return true
        }


        /*
         * Prevent the inventory key from interfering while the KCraft
         * computer is open.
         */
        if (
            event.key ==
            GLFW.GLFW_KEY_E
        ) {
            return true
        }


        /*
         * Anything we don't explicitly handle goes back to Minecraft.
         *
         * This remains important because AbstractContainerScreen is still
         * responsible for ordinary inventory interaction.
         */
        return super.keyPressed(
            event
        )
    }


    // -------------------------------------------------------------------------
    // Vanilla labels
    // -------------------------------------------------------------------------

    /*
     * Suppress Minecraft's default container title and inventory labels.
     *
     * KCraft draws its own interface instead.
     */
    override fun extractLabels(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int
    ) = Unit


    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {

        /*
         * ---------------------------------------------------------------------
         * Layout
         * ---------------------------------------------------------------------
         *
         * 400 × 190
         *
         * ┌────────────────────────────────────────────────────┐
         * │ ┌────────────────────────┐      Ruby Golem    [◇] │
         * │ │                        │                        │
         * │ │       TERMINAL         │     PLAYER INVENTORY   │
         * │ │                        │     □ □ □ □ □ □ □ □ □ │
         * │ │                        │     □ □ □ □ □ □ □ □ □ │
         * │ │                        │     □ □ □ □ □ □ □ □ □ │
         * │ │                        │                        │
         * │ │                        │     □ □ □ □ □ □ □ □ □ │
         * │ └────────────────────────┘                        │
         * └────────────────────────────────────────────────────┘
         */


        // ---------------------------------------------------------------------
        // Terminal panel
        // ---------------------------------------------------------------------

        val terminalLeft =
            leftPos + 8

        val terminalTop =
            topPos + 8

        val terminalRight =
            leftPos + 216

        val terminalBottom =
            topPos + 166


        /*
         * Slight border around the terminal.
         */
        graphics.fill(
            terminalLeft - 1,
            terminalTop - 1,
            terminalRight + 1,
            terminalBottom + 1,
            ARGB.opaque(
                0x404040
            )
        )


        /*
         * Dark terminal background.
         */
        graphics.fill(
            terminalLeft,
            terminalTop,
            terminalRight,
            terminalBottom,
            ARGB.opaque(
                0x101010
            )
        )


        // ---------------------------------------------------------------------
        // Player inventory panel
        // ---------------------------------------------------------------------

        /*
         * Temporary programmer-art background.
         *
         * We'll eventually replace this with a proper KCraft GUI.
         */
        graphics.fill(
            leftPos + 220,
            topPos + 76,
            leftPos + 396,
            topPos + 168,
            ARGB.opaque(
                0x202020
            )
        )


        // ---------------------------------------------------------------------
        // Ruby Golem slot
        // ---------------------------------------------------------------------

        /*
         * Use ComputerMenu's coordinates rather than duplicating the numbers.
         *
         * This means moving the actual slot automatically moves its visual
         * background as well.
         */
        val golemSlotX =
            leftPos +
                    ComputerMenu.GOLEM_SLOT_X

        val golemSlotY =
            topPos +
                    ComputerMenu.GOLEM_SLOT_Y


        /*
         * Outer slot border.
         */
        graphics.fill(
            golemSlotX - 1,
            golemSlotY - 1,
            golemSlotX + 19,
            golemSlotY + 19,
            ARGB.opaque(
                0x707070
            )
        )


        /*
         * Recessed slot background.
         */
        graphics.fill(
            golemSlotX,
            golemSlotY,
            golemSlotX + 18,
            golemSlotY + 18,
            ARGB.opaque(
                0x383838
            )
        )


        // ---------------------------------------------------------------------
        // Vanilla container rendering
        // ---------------------------------------------------------------------

        /*
         * Let Minecraft render the actual ItemStacks and manage container
         * state on top of our backgrounds.
         *
         * This handles:
         *
         *   - Gerald
         *   - player inventory items
         *   - carried ItemStacks
         *   - slot hover state
         *   - tooltips
         *   - ordinary container interactions
         */
        super.extractRenderState(
            graphics,
            mouseX,
            mouseY,
            partialTick
        )


        // ---------------------------------------------------------------------
        // Terminal text
        // ---------------------------------------------------------------------

        val terminalX =
            leftPos + 14

        val terminalY =
            topPos + 14


        for (
        (index, line)
        in visibleLines.withIndex()
        ) {
            graphics.text(
                font,
                Component.literal(
                    line
                ),
                terminalX,
                terminalY +
                        index * 12,
                ARGB.opaque(
                    0xFFFFFF
                )
            )
        }


        // ---------------------------------------------------------------------
        // Temporary labels
        // ---------------------------------------------------------------------

        /*
         * Just enough visual structure to make the programmer-art GUI
         * understandable.
         */

        graphics.text(
            font,
            Component.literal(
                "Ruby Golem"
            ),
            leftPos + 292,
            topPos + 24,
            ARGB.opaque(
                0xFFFFFF
            )
        )


        graphics.text(
            font,
            Component.literal(
                "Inventory"
            ),
            leftPos +
                    ComputerMenu.INVENTORY_X,
            topPos + 70,
            ARGB.opaque(
                0xFFFFFF
            )
        )
    }
}