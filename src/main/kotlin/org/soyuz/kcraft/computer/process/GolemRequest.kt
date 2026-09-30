package org.soyuz.kcraft.computer.process

import org.soyuz.kcraft.computer.api.minecraft.KCraftPosition
import java.util.concurrent.CompletableFuture

sealed interface GolemRequest : ComputerRequest {

    data class GetOwnedGolems(
        val result: CompletableFuture<List<GolemSnapshot>>
    ) : GolemRequest

    data class MoveTo(
        val golemId: String,
        val position: KCraftPosition,
        val result: CompletableFuture<Unit>
    ) : GolemRequest
}

data class GolemSnapshot(
    val id: String,
    val position: KCraftPosition
)