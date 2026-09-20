package org.soyuz.kcraft.client

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import net.minecraft.core.RegistrySetBuilder
import net.minecraft.core.registries.Registries
import org.soyuz.kcraft.client.datagen.KCraftBlockLootTableProvider
import org.soyuz.kcraft.client.datagen.KCraftWorldgenProvider
import org.soyuz.kcraft.worldgen.KCraftConfiguredFeatures
import org.soyuz.kcraft.worldgen.KCraftPlacedFeatures


class KCraftDataGenerator : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(
        fabricDataGenerator: FabricDataGenerator
    ) {

        val pack = fabricDataGenerator.createPack()

        pack.addProvider(
            ::KCraftBlockLootTableProvider
        )

        pack.addProvider(
            ::KCraftWorldgenProvider
        )
    }

    override fun buildRegistry(registryBuilder: RegistrySetBuilder) {
        registryBuilder.add(
            Registries.CONFIGURED_FEATURE,
            KCraftConfiguredFeatures::bootstrap
        )

        registryBuilder.add(
            Registries.PLACED_FEATURE,
            KCraftPlacedFeatures::bootstrap
        )
    }


}