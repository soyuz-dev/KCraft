package org.soyuz.kcraft.computer

interface ComputerMode {

    fun handleInput(
        input: ComputerInput,
        runtime: ComputerRuntime
    )

    fun displayState(): ComputerDisplayState
}