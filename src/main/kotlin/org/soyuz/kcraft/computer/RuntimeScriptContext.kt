package org.soyuz.kcraft.computer

import org.soyuz.kcraft.computer.api.KCraftScriptContext
import org.soyuz.kcraft.computer.api.KCraftTerminal

internal class RuntimeScriptContext(
    runtime: ComputerRuntime
) : KCraftScriptContext {

    override val terminal: KCraftTerminal =
        RuntimeScriptTerminal(runtime.terminal)
}