package org.soyuz.kcraft.computer.api

abstract class KCraftScript(
    val context: KCraftScriptContext
) {
    val terminal: KCraftTerminal
        get() = context.terminal

    val files: KCraftFileSystem
        get() = context.files
}