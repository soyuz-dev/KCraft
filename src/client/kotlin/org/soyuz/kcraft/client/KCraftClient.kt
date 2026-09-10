package org.soyuz.kcraft.client

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screens.MenuScreens
import org.soyuz.kcraft.KCraftMenus
import org.soyuz.kcraft.client.computer.ComputerScreen
import org.soyuz.kcraft.client.network.KCraftClientPackets

class KCraftClient : ClientModInitializer {

    override fun onInitializeClient() {
        MenuScreens.register(
            KCraftMenus.COMPUTER,
            ::ComputerScreen
        )
        KCraftClientPackets.initialize()
    }
}
