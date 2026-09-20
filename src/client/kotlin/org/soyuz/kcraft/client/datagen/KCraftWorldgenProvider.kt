package org.soyuz.kcraft.client.datagen

import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput
import net.fabricmc.fabric.api.datagen.v1.provider.FabricDynamicRegistryProvider
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import java.util.concurrent.CompletableFuture

class KCraftWorldgenProvider(
    output: FabricPackOutput,
    registries: CompletableFuture<HolderLookup.Provider>
) : FabricDynamicRegistryProvider(
    output,
    registries
) {

    override fun configure(
        registries: HolderLookup.Provider,
        entries: Entries
    ) {
        entries.addAll(
            registries.lookupOrThrow(
                Registries.CONFIGURED_FEATURE
            )
        )

        entries.addAll(
            registries.lookupOrThrow(
                Registries.PLACED_FEATURE
            )
        )
    }

    override fun getName(): String =
        "KCraft World Generation"
}