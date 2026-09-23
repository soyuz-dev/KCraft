package org.soyuz.kcraft.computer.process

import java.util.concurrent.CompletableFuture

sealed interface ComputerRequest {
    data class TerminalOutput(
        val text: String
    ) : ComputerRequest



}

