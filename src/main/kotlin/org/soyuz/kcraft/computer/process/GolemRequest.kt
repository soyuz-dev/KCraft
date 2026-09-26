package org.soyuz.kcraft.computer.process

import org.soyuz.kcraft.computer.api.minecraft.KCraftGolem
import java.util.concurrent.CompletableFuture

sealed interface GolemRequest : ComputerRequest {

    data class GetOwnedGolems(
        val result: CompletableFuture<List<KCraftGolem>>
    ) : GolemRequest
}