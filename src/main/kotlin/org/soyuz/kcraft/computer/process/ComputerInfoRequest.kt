package org.soyuz.kcraft.computer.process

import org.soyuz.kcraft.computer.api.minecraft.KCraftPosition
import java.util.concurrent.CompletableFuture

sealed interface ComputerInfoRequest: ComputerRequest {
    data class GetSelfPosition(
        val result: CompletableFuture<KCraftPosition>
    ) : ComputerInfoRequest
}