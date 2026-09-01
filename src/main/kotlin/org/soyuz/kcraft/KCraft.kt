package org.soyuz.kcraft

import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.slf4j.LoggerFactory

object KCraft : ModInitializer {
    const val MOD_ID: String = "kcraft"

    val LOGGER = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        KCraftItems.initialize()
        KCraftBlocks.initialize()
        LOGGER.info("KCraft Initialised")
    }

    fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)


}
