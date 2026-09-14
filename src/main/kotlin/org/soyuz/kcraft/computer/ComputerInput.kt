package org.soyuz.kcraft.computer

sealed interface ComputerInput {

    data class Character(
        val codepoint: Int
    ) : ComputerInput

    data object Enter : ComputerInput
    data object Backspace : ComputerInput

    data object Up : ComputerInput
    data object Down : ComputerInput
    data object Left : ComputerInput
    data object Right : ComputerInput

    data object Save : ComputerInput
    data object Exit : ComputerInput
}

