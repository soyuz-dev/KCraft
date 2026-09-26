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

    val world
        get() = context.world

    val redstone
        get() = context.redstone

    val computer
        get() = context.computer
}