package org.soyuz.kcraft.computer

class NanoMode(
    val path: String,
    private var contents: String
) : ComputerMode {

    override fun handleInput(
        input: ComputerInput,
        runtime: ComputerRuntime
    ) {
        when (input) {
            is ComputerInput.Character -> {
                // insert character
            }

            ComputerInput.Backspace -> {
                // delete character
            }

            ComputerInput.Save -> {
                runtime.fileSystem.writeFile(
                    path,
                    contents
                )
            }

            ComputerInput.Exit -> {
                runtime.returnToTerminal()
            }

            else -> Unit
        }
    }

    override fun displayState(): ComputerDisplayState {
        TODO()
    }
}