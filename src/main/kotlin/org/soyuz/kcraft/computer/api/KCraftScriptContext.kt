package org.soyuz.kcraft.computer.api

import org.soyuz.kcraft.computer.api.minecraft.KCraftWorld

interface KCraftScriptContext {
    val terminal: KCraftTerminal
    val files: KCraftFileSystem
    val env: KCraftEnvironment
    val world: KCraftWorld
    val computer: KCraftComputer
}