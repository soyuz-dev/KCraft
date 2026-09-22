package org.soyuz.kcraft.computer

import org.soyuz.kcraft.computer.api.KCraftTerminal

class RuntimeScriptTerminal(
    private val terminal: Terminal
) : KCraftTerminal {

    override fun println(text: String) {
        terminal.appendLine(text)
    }
}