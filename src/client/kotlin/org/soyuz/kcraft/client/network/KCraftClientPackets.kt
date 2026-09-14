package org.soyuz.kcraft.client.network

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import org.soyuz.kcraft.client.computer.ComputerScreen
import org.soyuz.kcraft.network.s2c.ComputerDisplayStatePayload

object KCraftClientPackets {
    fun initialize() {
        ClientPlayNetworking.registerGlobalReceiver(
            ComputerDisplayStatePayload.TYPE
        ) { payload, context ->

            val screen =
                context.client().gui.screen() as? ComputerScreen
                    ?: return@registerGlobalReceiver

            screen.updateTerminalState(
                lines = payload.lines,
                cursorRow = payload.cursorRow,
                cursorColumn = payload.cursorColumn
            )
        }
    }
}