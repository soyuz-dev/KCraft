package org.soyuz.kcraft.computer.ksh

import org.soyuz.kcraft.computer.ComputerRuntime
import kotlin.script.experimental.api.ScriptDiagnostic

class KShInterpreter(
    private val runtime: ComputerRuntime
) {

    private val terminal
        get() = runtime.terminal

    private val fileSystem
        get() = runtime.fileSystem

    companion object {
        private const val MAX_SOURCE_DEPTH = 16
    }

    private val commandDescriptions = mapOf(
        "echo" to "Print text to the terminal",
        "clear" to "Clear the terminal",
        "source" to "Execute a KSh script",

        "pwd" to "Print the current working directory",
        "cd" to "Change the working directory",
        "ls" to "List files in a directory",
        "cat" to "Print a file",

        "touch" to "Create a file",
        "mkdir" to "Create a directory",
        "rmdir" to "Remove an empty directory",
        "rm" to "Remove a file",

        "append" to "Append text to a file",
        "appendln" to "Append a line to a file",

        "pico" to "Edit a file with Pico"
    )

    fun executeLine(source: String) {
        executeLine(source, 0)
    }

    private fun executeLine(source: String, sourceDepth: Int) {
        val tokens = KShLexer.lex(source)

        if (tokens.isEmpty()) return

        val command = tokens.first().value
        val args = tokens
            .drop(1)
            .map { expand(it.value) }

        when (command) {
            "echo" -> echo(args)
            "clear" -> clear(args)
            "source" -> source(args, sourceDepth)
            "run" -> run(args)

            "touch" -> touch(args)
            "mkdir" -> mkdir(args)
            "cd" -> cd(args)
            "pwd" -> pwd(args)

            "append" -> append(args)
            "appendln" -> append(args, newline = true)

            "ls" -> ls(args)
            "cat" -> cat(args)
            "rm" -> rm(args)
            "rmdir" -> rmdir(args)

            "pico" -> pico(args)

            "help" -> help(args)


            else -> terminal.appendLine(
                "ksh: command not found: $command"
            )
        }
    }

    private fun echo(args: List<String>) {
        terminal.appendLine(args.joinToString(" "))
    }

    private fun clear(args: List<String>) {
        if (args.isNotEmpty()) {
            terminal.appendLine("clear: expected no arguments")
            return
        }

        terminal.clear()
    }
    private fun source(
        args: List<String>,
        depth: Int
    ) {
        if (args.size != 1) {
            terminal.appendLine(
                "source: expected exactly one path"
            )
            return
        }

        if (depth >= MAX_SOURCE_DEPTH) {
            terminal.appendLine(
                "source: drowning in the depths"
            )
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.single()
        )

        if (!fileSystem.exists(path)) {
            terminal.appendLine(
                "source: file not found: $path"
            )
            return
        }

        if (fileSystem.isDirectory(path)) {
            terminal.appendLine(
                "source: is a directory: $path"
            )
            return
        }

        val contents = fileSystem.readFile(path)

        contents.lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .filterNot { it.startsWith("#") }
            .forEach { line ->
                executeLine(
                    line,
                    depth + 1
                )
            }
    }
    private fun touch(args: List<String>) {
        if (args.size != 1) {
            terminal.appendLine("touch: expected one path")
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.single()
        )

        try {
            fileSystem.createFile(path)
        } catch (e: IllegalArgumentException) {
            terminal.appendLine("touch: ${e.message}")
        }
    }

    private fun mkdir(args: List<String>) {
        if (args.size != 1) {
            terminal.appendLine("mkdir: expected one path")
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.single()
        )

        try {
            fileSystem.createDirectory(path)
        } catch (e: IllegalArgumentException) {
            terminal.appendLine("mkdir: ${e.message}")
        }
    }

    private fun cd(args: List<String>) {
        if (args.size != 1) {
            terminal.appendLine("cd: expected one path")
            return
        }

        val target = args.single()

        val normalized = fileSystem.normalizePath(
            runtime.workingDirectory,
            target
        )

        if (!fileSystem.exists(normalized)) {
            terminal.appendLine("cd: no such file or directory: $target")
            return
        }

        if (!fileSystem.isDirectory(normalized)) {
            terminal.appendLine("cd: not a directory: $target")
            return
        }

        runtime.changeDirectory(target)
    }

    private fun pwd(args: List<String>) {
        if (args.isNotEmpty()) {
            terminal.appendLine("pwd: expected no arguments")
            return
        }

        terminal.appendLine(runtime.workingDirectory)
    }

    private fun append(
        args: List<String>,
        newline: Boolean = false
    ) {
        if (args.size < 2) {
            terminal.appendLine(
                "append: expected path and text"
            )
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.first()
        )

        val text = args
            .drop(1)
            .joinToString(" ")
            .let { if (newline) "$it\n" else it }

        try {
            fileSystem.appendFile(path, text)
        } catch (e: IllegalArgumentException) {
            terminal.appendLine("append: ${e.message}")
        }
    }

    private fun expand(value: String): String {
        if (!value.startsWith("$")) {
            return value
        }

        val name = value.drop(1)

        return runtime.environment.get(name) ?: ""
    }

    private fun pico(args: List<String>) {
        if (args.size != 1) {
            terminal.appendLine(
                "pico: expected one path"
            )
            return
        }

        runtime.openPico(
            args.single()
        )
    }

    private fun ls(args: List<String>) {
        if (args.size > 1) {
            terminal.appendLine("ls: expected at most one path")
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.firstOrNull() ?: "."
        )

        if (!fileSystem.exists(path)) {
            terminal.appendLine("ls: path not found: $path")
            return
        }

        if (!fileSystem.isDirectory(path)) {
            terminal.appendLine("ls: not a directory: $path")
            return
        }

        fileSystem.list(path)
            .sorted()
            .forEach(terminal::appendLine)
    }

    private fun rm(args: List<String>) {
        if (args.size != 1) {
            terminal.appendLine("rm: expected one path")
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.single()
        )

        try {
            fileSystem.deleteFile(path)
        } catch (e: IllegalArgumentException) {
            terminal.appendLine("del: ${e.message}")
        }
    }

    private fun rmdir(args: List<String>) {
        if (args.size != 1) {
            terminal.appendLine("rmdir: expected one path")
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.single()
        )

        try {
            fileSystem.deleteDirectory(path)
        } catch (e: IllegalArgumentException) {
            terminal.appendLine("rmdir: ${e.message}")
        }
    }

    private fun cat(args: List<String>) {
        if (args.size != 1) {
            terminal.appendLine("cat: expected one path")
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.single()
        )

        if (!fileSystem.exists(path)) {
            terminal.appendLine("cat: file not found: $path")
            return
        }

        if (fileSystem.isDirectory(path)) {
            terminal.appendLine("cat: is a directory: $path")
            return
        }

        fileSystem.readFile(path)
            .lineSequence()
            .forEach(terminal::appendLine)
    }

    private fun help(args: List<String>) {
        when (args.size) {
            0 -> {
                terminal.appendLine("KSh commands:")

                commandDescriptions.keys
                    .sorted()
                    .forEach { command ->
                        terminal.appendLine(
                            "$command - ${commandDescriptions.getValue(command)}"
                        )
                    }
            }

            1 -> {
                val command = args.single()
                val description = commandDescriptions[command]

                if (description == null) {
                    terminal.appendLine(
                        "help: no such command: $command"
                    )
                    return
                }

                terminal.appendLine(
                    "$command - $description"
                )
            }

            else ->
                terminal.appendLine(
                    "help: expected at most one command"
                )
        }
    }

    private fun run(args: List<String>) {
        if (args.size != 1) {
            terminal.appendLine(
                "run: expected exactly one path"
            )
            return
        }

        val path = fileSystem.normalizePath(
            runtime.workingDirectory,
            args.single()
        )

        if (!fileSystem.exists(path)) {
            terminal.appendLine(
                "run: file not found: $path"
            )
            return
        }

        if (fileSystem.isDirectory(path)) {
            terminal.appendLine(
                "run: is a directory: $path"
            )
            return
        }

        if (!path.endsWith(".kts")) {
            terminal.appendLine(
                "run: expected a .kts file"
            )
            return
        }

        val source = fileSystem.readFile(path)

        val result = runtime.kotlin.execute(source)

        result.reports
            .filter { report ->
                report.severity >=
                        ScriptDiagnostic.Severity.WARNING
            }
            .forEach { report ->
                terminal.appendLine(
                    formatDiagnostic(
                        path,
                        report
                    )
                )
            }
    }

    private fun formatDiagnostic(
        path: String,
        diagnostic: ScriptDiagnostic
    ): String {
        val location = diagnostic.location

        return if (location != null) {
            "$path:${location.start.line}:${location.start.col}: " +
                    diagnostic.message
        } else {
            "$path: ${diagnostic.message}"
        }
    }
}