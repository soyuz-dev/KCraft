package org.soyuz.kcraft.worldgen

import net.minecraft.core.registries.Registries
import net.minecraft.data.worldgen.BootstrapContext
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.levelgen.VerticalAnchor
import net.minecraft.world.level.levelgen.placement.*
import org.soyuz.kcraft.KCraft

object KCraftPlacedFeatures {

    val RUBY_ORE = ResourceKey.create(
        Registries.PLACED_FEATURE,
        KCraft.id("ruby_ore")
    )

    fun bootstrap(
        context: BootstrapContext<PlacedFeature>
    ) {
        val configuredFeatures =
            context.lookup(Registries.CONFIGURED_FEATURE)

        val modifiers = listOf(
            CountPlacement.of(7),
            InSquarePlacement.spread(),

            HeightRangePlacement.uniform(
                VerticalAnchor.absolute(5),
                VerticalAnchor.absolute(100)
            ),

            BiomeFilter.biome()
        )

        context.register(
            RUBY_ORE,
            PlacedFeature(
                configuredFeatures.getOrThrow(
                    KCraftConfiguredFeatures.RUBY_ORE
                ),
                modifiers
            )
        )
    }
}