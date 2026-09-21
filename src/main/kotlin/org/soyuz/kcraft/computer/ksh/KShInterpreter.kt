package org.soyuz.kcraft.computer.ksh

import org.soyuz.kcraft.computer.ComputerRuntime

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
            "touch" -> touch(args)
            "mkdir" -> mkdir(args)
            "cd" -> cd(args)
            "pwd" -> pwd(args)
            "append" -> append(args)
            "appendln" -> append(args, newline = true)
            "pico" -> pico(args)


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

    private fun source(args: List<String>, depth: Int) {
        if (args.size != 1) {
            terminal.appendLine("source: expected exactly one path")
            return
        }
        if (depth > MAX_SOURCE_DEPTH) {
            terminal.appendLine("source: drowning in the depths")
            return
        }

        val path = args.single()

        if (!fileSystem.exists(path)) {
            terminal.appendLine("source: file not found: $path")
            return
        }

        val contents = fileSystem.readFile(path)

        contents.lineSequence()
            .map(String::trim)
            .filter(String::isNotEmpty)
            .filterNot { it.startsWith("#") }
            .forEach { line ->
                executeLine(line, depth + 1)
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
}