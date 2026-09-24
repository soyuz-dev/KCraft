package org.soyuz.kcraft.computer.process

import org.soyuz.kcraft.computer.api.minecraft.KCraftBlock
import java.util.concurrent.CompletableFuture

sealed interface WorldRequest : ComputerRequest {

    data class GetBlock(
        val x: Int,
        val y: Int,
        val z: Int,
        val result: CompletableFuture<KCraftBlock>
    ) : WorldRequest

}