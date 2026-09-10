package org.soyuz.kcraft.network

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.soyuz.kcraft.computer.ComputerMenu
import org.soyuz.kcraft.network.c2s.RequestTerminalStatePayload
import org.soyuz.kcraft.network.c2s.TerminalInputPayload
import org.soyuz.kcraft.network.s2c.TerminalStatePayload

object KCraftPackets {
    fun initialize() {
        PayloadTypeRegistry.clientboundPlay().register(TerminalStatePayload.TYPE, TerminalStatePayload.STREAM_CODEC)
        PayloadTypeRegistry.serverboundPlay().register(TerminalInputPayload.TYPE, TerminalInputPayload.STREAM_CODEC)
        PayloadTypeRegistry.serverboundPlay().register(RequestTerminalStatePayload.TYPE,RequestTerminalStatePayload.STREAM_CODEC)

        ServerPlayNetworking.registerGlobalReceiver(
            TerminalInputPayload.TYPE
        ) { payload, context ->

            val player = context.player()

            val menu = player.containerMenu as? ComputerMenu
                ?: return@registerGlobalReceiver

            val computer = menu.computer
                ?: return@registerGlobalReceiver

            computer.handleInput(
                player,
                payload
            )
        }

        ServerPlayNetworking.registerGlobalReceiver(
            RequestTerminalStatePayload.TYPE
        ) { _, context ->
            val player = context.player()

            val menu = player.containerMenu as? ComputerMenu
                ?: return@registerGlobalReceiver

            val computer = menu.computer
                ?: return@registerGlobalReceiver

            computer.syncTerminal(player)
        }
    }
}