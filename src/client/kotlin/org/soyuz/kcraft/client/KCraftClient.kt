package org.soyuz.kcraft.client

import com.geckolib.renderer.GeoEntityRenderer
import net.fabricmc.api.ClientModInitializer
import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.entity.EntityRenderers
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import org.soyuz.kcraft.KCraftEntities
import org.soyuz.kcraft.KCraftMenus
import org.soyuz.kcraft.client.computer.ComputerScreen
import org.soyuz.kcraft.client.network.KCraftClientPackets
import org.soyuz.kcraft.golem.RubyGolem

class KCraftClient : ClientModInitializer {

    override fun onInitializeClient() {
        MenuScreens.register(
            KCraftMenus.COMPUTER,
            ::ComputerScreen
        )
        KCraftClientPackets.initialize()

        EntityRenderers.register(
            KCraftEntities.RUBY_GOLEM
        ) { context ->
            GeoEntityRenderer<
                    RubyGolem,
                    LivingEntityRenderState
                    >(
                context,
                KCraftEntities.RUBY_GOLEM
            )
        }
    }
}
