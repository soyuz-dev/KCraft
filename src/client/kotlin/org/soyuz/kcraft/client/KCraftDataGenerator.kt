package org.soyuz.kcraft.client

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator
import org.soyuz.kcraft.client.datagen.KCraftBlockLootTableProvider


class KCraftDataGenerator : DataGeneratorEntrypoint {

    override fun onInitializeDataGenerator(
        fabricDataGenerator: FabricDataGenerator
    ) {

        val pack = fabricDataGenerator.createPack()

        pack.addProvider { output, registries ->

            KCraftBlockLootTableProvider(
                output,
                registries
            )
        }
    }


}