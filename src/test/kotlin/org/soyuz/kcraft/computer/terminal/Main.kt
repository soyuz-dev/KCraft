package org.soyuz.kcraft.computer.terminal

import java.util.Scanner

fun main() {
    val terminal = Terminal()
    val scanner = Scanner(System.`in`)

    println("--- TERMINAL MANUAL TEST SANDBOX ---")
    println("Controls:")
    println("  [Text]      -> Type characters")
    println("  [Empty/Enter] -> Create a new line")
    println("  \\b          -> Simulate Backspace")
    println("  exit        -> Quit Sandbox\n")

    while (true) {
        val cursor = terminal.cursorPosition
        val visibleLines = terminal.visibleLines

        println("=".repeat(Terminal.MAX_LINE_LENGTH))
        for (rowIndex in 0 until Terminal.VISIBLE_LINES) {
            println(visibleLines[rowIndex])
        }
        println("=".repeat(Terminal.MAX_LINE_LENGTH))
        println("Lines: ${terminal.lineCount} | Cursor: $cursor")
        print("> ")

        val input = scanner.nextLine()

        if (input == "exit") break

        when {
            // 1. Empty string means the user just pressed Enter
            input.isEmpty() -> {
                terminal.appendLine("")
            }
            // 2. Explicit simulation string for backspace
            input == "\\b" -> {
                terminal.popChar()
            }
            // 3. Process normal character inputs
            else -> {
                input.forEach { terminal.appendChar(it) }
            }
        }
    }
}
