package org.soyuz.kcraft.worldgen

import net.fabricmc.fabric.api.biome.v1.BiomeModifications
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors
import net.minecraft.world.level.levelgen.GenerationStep

object KCraftWorldGeneration {

    fun initialize() {
        BiomeModifications.addFeature(
            BiomeSelectors.foundInTheNether(),
            GenerationStep.Decoration.UNDERGROUND_ORES,
            KCraftPlacedFeatures.RUBY_ORE
        )
    }
}