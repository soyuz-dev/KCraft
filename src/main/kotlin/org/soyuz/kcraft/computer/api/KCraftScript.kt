package org.soyuz.kcraft.computer.api

abstract class KCraftScript(
    val context: KCraftScriptContext
) {
    val terminal
        get() = context.terminal

    val files
        get() = context.files

    val env
        get() = context.env
}