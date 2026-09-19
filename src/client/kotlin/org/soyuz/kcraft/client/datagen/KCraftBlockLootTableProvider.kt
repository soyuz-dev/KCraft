package org.soyuz.kcraft.client.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricBlockLootSubProvider
import net.minecraft.core.HolderLookup
import org.soyuz.kcraft.KCraftBlocks
import org.soyuz.kcraft.KCraftItems
import java.util.concurrent.CompletableFuture

class KCraftBlockLootTableProvider(
    output: FabricPackOutput,
    registries: CompletableFuture<HolderLookup.Provider>
) : FabricBlockLootSubProvider(
    output,
    registries
) {

    override fun generate() {

        add(
            KCraftBlocks.RUBY_ORE,
            createOreDrop(
                KCraftBlocks.RUBY_ORE,
                KCraftItems.RUBY
            )
        )
    }
}