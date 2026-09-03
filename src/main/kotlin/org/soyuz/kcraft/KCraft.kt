package org.soyuz.kcraft

import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory

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
        LOGGER.info("KCraft Initialised")
    }

}
