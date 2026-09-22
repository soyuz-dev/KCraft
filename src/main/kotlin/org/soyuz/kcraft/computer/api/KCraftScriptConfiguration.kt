package org.soyuz.kcraft.computer.api

import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.jvm.dependenciesFromCurrentContext
import kotlin.script.experimental.jvm.jvm

object KCraftScriptCompilationConfiguration :
    ScriptCompilationConfiguration({

        baseClass(KCraftScript::class)

        jvm {
            // Prototype only.
            // This exposes the host classpath to script compilation.
            // Replace with an explicit KCraft scripting classpath before
            // treating player-provided scripts as untrusted.
            // DO NOT PUBLISH THIS WITHOUT WARNING.
            dependenciesFromCurrentContext(
                wholeClasspath = true
            )
        }
    }) {
}