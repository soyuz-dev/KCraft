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
import org.soyuz.kcraft.network.c2s.RequestTerminalStatePayload
import org.soyuz.kcraft.network.c2s.TerminalInputPayload

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
            RequestTerminalStatePayload
        )
    }

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

    override fun charTyped(event: CharacterEvent): Boolean {
        ClientPlayNetworking.send(
            TerminalInputPayload(
                type = TerminalInputPayload.Type.CHARACTER,
                character = event.codepoint
            )
        )

        return true
    }

    override fun keyPressed(event: KeyEvent): Boolean {
        when (event.key) {
            GLFW.GLFW_KEY_ENTER -> {
                ClientPlayNetworking.send(
                    TerminalInputPayload(
                        TerminalInputPayload.Type.ENTER
                    )
                )
                return true
            }

            GLFW.GLFW_KEY_BACKSPACE -> {
                ClientPlayNetworking.send(
                    TerminalInputPayload(
                        TerminalInputPayload.Type.BACKSPACE
                    )
                )
                return true
            }

            GLFW.GLFW_KEY_UP -> {
                ClientPlayNetworking.send(
                    TerminalInputPayload(
                        TerminalInputPayload.Type.SCROLL_UP
                    )
                )
                return true
            }

            GLFW.GLFW_KEY_DOWN -> {
                ClientPlayNetworking.send(
                    TerminalInputPayload(
                        TerminalInputPayload.Type.SCROLL_DOWN
                    )
                )
                return true
            }

            GLFW.GLFW_KEY_E -> {
                // prevent inventory key from closing/opening over the screen
                return true
            }
        }

        return super.keyPressed(event)
    }

    override fun extractLabels(graphics: GuiGraphicsExtractor, xm: Int, ym: Int) = Unit

    override fun extractRenderState(
        graphics: GuiGraphicsExtractor,
        mouseX: Int,
        mouseY: Int,
        partialTick: Float
    ) {
        super.extractRenderState(
            graphics,
            mouseX,
            mouseY,
            partialTick
        )

        val startX = 20
        val startY = 20

        for ((index, line) in visibleLines.withIndex()) {
            graphics.text(
                font,
                Component.literal(line),
                startX,
                startY + index * 12,
                ARGB.opaque(0xFFFFFF)
            )
        }
    }

    // rendering later
}