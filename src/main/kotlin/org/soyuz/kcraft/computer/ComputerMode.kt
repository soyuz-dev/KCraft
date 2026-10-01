package org.soyuz.kcraft.computer

interface ComputerMode {

    companion object {
        /*
         * Physical dimensions of KCraft's text display.
         *
         * These describe visual rows/columns, not the maximum size
         * of a logical line in Terminal or Pico.
         */
        const val DISPLAY_WIDTH = 40
        const val VISIBLE_LINES = 12
    }

    fun handleInput(
        input: ComputerInput,
        runtime: ComputerRuntime
    )

    fun displayState(): ComputerDisplayState
}