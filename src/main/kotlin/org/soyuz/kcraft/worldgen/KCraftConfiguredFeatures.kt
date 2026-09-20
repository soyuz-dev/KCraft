package org.soyuz.kcraft.worldgen

import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature
import net.minecraft.world.level.levelgen.feature.Feature
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest
import org.soyuz.kcraft.KCraft
import org.soyuz.kcraft.KCraftBlocks

object KCraftConfiguredFeatures {

    val RUBY_ORE = ResourceKey.create(
        Registries.CONFIGURED_FEATURE,
        KCraft.id("ruby_ore")
    )

    fun bootstrap(
        context: BootstrapContext<ConfiguredFeature<*, *>>
    ) {
        val targets = listOf(
            OreConfiguration.target(
                BlockMatchTest(Blocks.BLACKSTONE),
                KCraftBlocks.RUBY_ORE.defaultBlockState()
            )
        )

        context.register(
            RUBY_ORE,
            ConfiguredFeature(
                Feature.ORE,
                OreConfiguration(
                    targets,
                    4
                )
            )
        )
    }
}