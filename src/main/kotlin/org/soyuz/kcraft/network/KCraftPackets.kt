package org.soyuz.kcraft.network

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.soyuz.kcraft.computer.ComputerMenu
import org.soyuz.kcraft.network.c2s.RequestComputerDisplayStatePayload
import org.soyuz.kcraft.network.c2s.ComputerInputPayload
import org.soyuz.kcraft.network.c2s.toComputerInput
import org.soyuz.kcraft.network.s2c.ComputerDisplayStatePayload

object KCraftPackets {
    fun initialize() {
        PayloadTypeRegistry.clientboundPlay().register(ComputerDisplayStatePayload.TYPE, ComputerDisplayStatePayload.STREAM_CODEC)
        PayloadTypeRegistry.serverboundPlay().register(ComputerInputPayload.TYPE, ComputerInputPayload.STREAM_CODEC)
        PayloadTypeRegistry.serverboundPlay().register(RequestComputerDisplayStatePayload.TYPE,RequestComputerDisplayStatePayload.STREAM_CODEC)

        ServerPlayNetworking.registerGlobalReceiver(
            ComputerInputPayload.TYPE
        ) { payload, context ->
            val player = context.player()

            val menu = player.containerMenu as? ComputerMenu
                ?: return@registerGlobalReceiver

            val computer = menu.computer
                ?: return@registerGlobalReceiver

            if (!computer.isViewer(player)) {
                return@registerGlobalReceiver
            }

            computer.runtime.handleInput(
                payload.toComputerInput()
            )

            computer.syncDisplay()
        }

        ServerPlayNetworking.registerGlobalReceiver(
            RequestComputerDisplayStatePayload.TYPE
        ) { _, context ->
            val player = context.player()

            val menu = player.containerMenu as? ComputerMenu
                ?: return@registerGlobalReceiver

            val computer = menu.computer
                ?: return@registerGlobalReceiver

            computer.syncDisplay(player)
        }
    }
}