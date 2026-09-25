package org.soyuz.kcraft

import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory
import org.soyuz.kcraft.computer.api.KCraftScript
import org.soyuz.kcraft.computer.api.KCraftScriptContext
import org.soyuz.kcraft.network.KCraftPackets
import org.soyuz.kcraft.worldgen.KCraftWorldGeneration

class KCraft : ModInitializer {
    companion object {
        const val MOD_ID: String = "kcraft"

        fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)
        val LOGGER = LoggerFactory.getLogger(MOD_ID)!!

    }

    override fun onInitialize() {
        KCraftItems.initialize()
        KCraftBlocks.initialize()
        KCraftBlockEntities.initialize()
        KCraftPackets.initialize()
        KCraftMenus.initialize()
        KCraftWorldGeneration.initialize()
        KCraftEntities.initialize()

        LOGGER.info("KCraft Initialised")
    }

}
