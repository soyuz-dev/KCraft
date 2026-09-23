package org.soyuz.kcraft.computer.process

import java.util.concurrent.CompletableFuture

sealed interface ComputerRequest {
    data class TerminalOutput(
        val text: String
    ) : ComputerRequest

    data class WriteFile(
        val path: String,
        val content: String,
        val result: CompletableFuture<Unit>
    ) : ComputerRequest
}