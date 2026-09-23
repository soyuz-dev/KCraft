package org.soyuz.kcraft.computer

import org.soyuz.kcraft.computer.api.KotlinScriptRuntime
import org.soyuz.kcraft.computer.ksh.KShEnvironment
import org.soyuz.kcraft.computer.ksh.KShInterpreter
import org.soyuz.kcraft.computer.process.ComputerRequest
import org.soyuz.kcraft.computer.process.KCraftProcessManager
import java.util.concurrent.ConcurrentLinkedQueue

class ComputerRuntime(
    val terminal: Terminal,
    val fileSystem: FileSystem
) {

    var activeMode: ComputerMode = terminal
        private set

    var workingDirectory: String = "/"
        private set

    var uptimeTicks = 0L
        private set

    private var booted = false

    val environment = KShEnvironment(this)
    val interpreter = KShInterpreter(this)
    val kotlin = KotlinScriptRuntime(RuntimeScriptContext(this))
    val processes = KCraftProcessManager(this)
    private val requests =
        ConcurrentLinkedQueue<ComputerRequest>()

    fun submitRequest(request: ComputerRequest) {
        requests.add(request)
    }

    fun tick(): Boolean{
        if (!booted) return false
        uptimeTicks++

        return processRequests()
    }

    fun boot() {
        if (booted) return

        booted = true

        if (fileSystem.exists("/etc/shell.kshrc")) {
            interpreter.executeLine(
                "source /etc/shell.kshrc"
            )
        }
    }

    fun handleInput(input: ComputerInput) {
        activeMode.handleInput(
            input,
            this
        )
    }

    fun displayState(): ComputerDisplayState =
        activeMode.displayState()

    fun submitCurrentCommand() {
        val command = terminal
            .commitInput()
            .trim()

        if (command.isNotEmpty()) {
            interpreter.executeLine(command)
        }
    }

    fun changeDirectory(path: String): Boolean {
        val normalized = fileSystem.normalizePath(
            workingDirectory,
            path
        )

        if (!fileSystem.exists(normalized)) return false
        if (!fileSystem.isDirectory(normalized)) return false

        workingDirectory = normalized
        return true
    }

    fun openPico(path: String) {
        val normalized = fileSystem.normalizePath(
            workingDirectory,
            path
        )

        if (
            fileSystem.exists(normalized) &&
            fileSystem.isDirectory(normalized)
        ) {
            terminal.appendLine(
                "pico: is a directory: $path"
            )
            return
        }

        val contents =
            if (fileSystem.exists(normalized)) {
                fileSystem.readFile(normalized)
            } else {
                ""
            }

        activeMode = PicoMode(
            path = normalized,
            contents = contents
        )
    }

    fun returnToTerminal() {
        activeMode = terminal
    }

    private fun processRequests(): Boolean {
        var changed = false

        while (true) {
            when (val request = requests.poll() ?: break) {
                is ComputerRequest.TerminalOutput -> {
                    terminal.appendLine(request.text)
                    changed = true
                }

                else -> {
                    terminal.appendLine("TODO")
                }
            }
        }

        return changed
    }
}