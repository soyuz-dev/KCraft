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
    title
) {

    init {
        ClientPlayNetworking.send(
            RequestComputerDisplayStatePayload
        )
    }


    // -------------------------------------------------------------------------
    // Terminal state
    // -------------------------------------------------------------------------

    private var visibleLines =
        List(Terminal.VISIBLE_LINES) { "" }

    private var cursorRow = 0
    private var cursorColumn = 0

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
    // Input
    // -------------------------------------------------------------------------

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

        if (input != null) {
            ClientPlayNetworking.send(
                ComputerInputPayload(
                    input
                )
            )

            return true
        }

        // Keep Minecraft's inventory key from
        // interfering with the computer.
        if (
            event.key ==
            GLFW.GLFW_KEY_E
        ) {
            return true
        }

        return super.keyPressed(event)
    }


    // -------------------------------------------------------------------------
    // Rendering
    // -------------------------------------------------------------------------

    override fun extractLabels(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int
    ) = Unit

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        /*
         * Let AbstractContainerScreen render and manage
         * the menu slots first.
         */
        super.extractRenderState(
            graphics,
            mouseX,
            mouseY,
            partialTick
        )

        /*
         * Terminal coordinates are screen coordinates here,
         * so offset them by the GUI's top-left corner.
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
                Component.literal(line),
                terminalX,
                terminalY +
                        index * 12,
                ARGB.opaque(
                    0xFFFFFF
                )
            )
        }
    }
}