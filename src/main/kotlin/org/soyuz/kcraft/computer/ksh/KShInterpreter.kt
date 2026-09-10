package org.soyuz.kcraft.computer.ksh

import org.soyuz.kcraft.computer.FileSystem
import org.soyuz.kcraft.computer.Terminal

class KShInterpreter(
    private val terminal: Terminal,
    private val fileSystem: FileSystem
) {

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
        val args = tokens.drop(1).map { it.value }

        when (command) {
            "echo" -> echo(args)
            "clear" -> clear(args)
            "source" -> source(args, sourceDepth)

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
}