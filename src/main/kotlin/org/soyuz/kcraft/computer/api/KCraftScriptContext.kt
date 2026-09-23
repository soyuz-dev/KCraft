package org.soyuz.kcraft.computer.api

interface KCraftScriptContext {
    val terminal: KCraftTerminal
    val files: KCraftFileSystem
}