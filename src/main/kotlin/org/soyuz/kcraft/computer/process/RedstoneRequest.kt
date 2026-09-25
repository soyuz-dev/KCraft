package org.soyuz.kcraft.computer.process

import org.soyuz.kcraft.computer.api.minecraft.KCraftDirection
import java.util.concurrent.CompletableFuture

sealed interface RedstoneRequest : ComputerRequest {

    data class Read(
        val direction: KCraftDirection,
        val result: CompletableFuture<Int>
    ) : RedstoneRequest

    data class Write(
        val direction: KCraftDirection,
        val strength: Int,
        val result: CompletableFuture<Unit>
    ) : RedstoneRequest
}