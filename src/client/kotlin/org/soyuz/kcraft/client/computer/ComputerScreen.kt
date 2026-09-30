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

    // Width and height of the container GUI.
    //
    // These are constructor arguments in the current Minecraft API;
    // imageWidth/imageHeight themselves do not have setters.
    //
    // 176 is the usual vanilla container width.
    // 250 gives KCraft enough vertical space for:
    //
    //   - the terminal
    //   - the Ruby Golem slot
    //   - the player's 3 inventory rows
    //   - the player's hotbar
    176,
    250
) {

    init {
        /*
         * Ask the server for the current terminal contents when the
         * computer screen first opens.
         *
         * Inventory slots do NOT need custom networking here.
         * AbstractContainerMenu already synchronises those.
         */
        ClientPlayNetworking.send(
            RequestComputerDisplayStatePayload
        )
    }


    // -------------------------------------------------------------------------
    // Terminal state
    // -------------------------------------------------------------------------

    /*
     * This is only the client-side representation of the terminal display.
     *
     * The authoritative terminal still lives in ComputerRuntime on the
     * server. The server sends us snapshots of what should currently be
     * visible.
     */
    private var visibleLines =
        List(Terminal.VISIBLE_LINES) { "" }

    private var cursorRow = 0
    private var cursorColumn = 0


    /**
     * Called when the server sends an updated terminal display.
     */
    fun updateTerminalState(
        lines: List<String>,
        cursorRow: Int,
        cursorColumn: Int
    ) {
        this.visibleLines = lines
        this.cursorRow = cursorRow
        this.cursorColumn = cursorColumn
    }


    // -------------------------------------------------------------------------
    // Keyboard input
    // -------------------------------------------------------------------------

    /**
     * Ordinary typed characters are sent to the server-side computer.
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


    /**
     * Translate keyboard controls into KCraft computer input.
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
         * If KCraft recognised the key, consume it so Minecraft does not
         * also try to interpret it as ordinary container input.
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
         * Normally E closes an inventory screen.
         *
         * KCraft currently treats the computer screen more like a terminal,
         * so keep E from interfering with terminal usage.
         */
        if (
            event.key ==
            GLFW.GLFW_KEY_E
        ) {
            return true
        }


        /*
         * Anything KCraft does not care about is handed back to
         * AbstractContainerScreen.
         *
         * This is important now that we have real inventory slots:
         * Minecraft still needs to handle its ordinary container controls.
         */
        return super.keyPressed(
            event
        )
    }


    // -------------------------------------------------------------------------
    // Vanilla labels
    // -------------------------------------------------------------------------

    /**
     * Suppress AbstractContainerScreen's normal title and inventory labels.
     *
     * KCraft has its own terminal-style interface instead.
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
         * VERY IMPORTANT:
         *
         * Let AbstractContainerScreen do its work.
         *
         * This is what gives us normal Minecraft container behaviour such as:
         *
         *   - rendering ItemStacks in slots
         *   - slot hover state
         *   - carried ItemStack rendering
         *   - tooltips
         *   - normal container interaction state
         *
         * The actual slot definitions live in ComputerMenu.
         */
        // ---------------------------------------------------------------------
// Temporary Ruby Golem slot background
// ---------------------------------------------------------------------

        val golemSlotX =
            leftPos + 151

        val golemSlotY =
            topPos + 20

        /*
         * Draw a simple dark 18 × 18 square behind the Ruby Golem slot.
         *
         * The actual interactive slot still belongs to ComputerMenu;
         * this is purely visual.
         */
        graphics.fill(
            golemSlotX,
            golemSlotY,
            golemSlotX + 18,
            golemSlotY + 18,
            ARGB.opaque(0x383838)
        )
        super.extractRenderState(
            graphics,
            mouseX,
            mouseY,
            partialTick
        )


        /*
         * Terminal rendering.
         *
         * leftPos/topPos are the top-left corner of the 176 × 250
         * container area on the player's screen.
         *
         * Keeping the terminal relative to these coordinates means the
         * terminal and the menu slots move together when Minecraft centres
         * the GUI on different screen sizes.
         */
        val terminalX =
            leftPos + 20

        val terminalY =
            topPos + 20


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


        /*
         * We deliberately are NOT drawing custom slot backgrounds yet.
         *
         * ComputerMenu provides the functional slots and Minecraft handles
         * the items. Once the inventory behaviour is confirmed to work,
         * this screen can get an actual KCraft GUI/background.
         */
    }
}