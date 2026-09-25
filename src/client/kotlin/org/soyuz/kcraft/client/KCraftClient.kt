package org.soyuz.kcraft.client

import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.entity.EntityRenderers
import org.soyuz.kcraft.KCraftEntities
import org.soyuz.kcraft.KCraftMenus
import org.soyuz.kcraft.client.computer.ComputerScreen
import org.soyuz.kcraft.client.golem.RubyGolemRenderer
import org.soyuz.kcraft.client.network.KCraftClientPackets

class KCraftClient : ClientModInitializer {

    override fun onInitializeClient() {
        MenuScreens.register(
            KCraftMenus.COMPUTER,
            ::ComputerScreen
        )
        KCraftClientPackets.initialize()

        EntityRenderers.register(
            KCraftEntities.RUBY_GOLEM,
            ::RubyGolemRenderer
        )
    }
}
