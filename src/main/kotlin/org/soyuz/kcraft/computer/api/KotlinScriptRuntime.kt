package org.soyuz.kcraft.computer.api

import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class KotlinScriptRuntime(
    private val context: KCraftScriptContext
) {

    private val host = BasicJvmScriptingHost()

    fun execute(source: String): ResultWithDiagnostics<*> {
        return host.eval(
            source.toScriptSource(),
            KCraftScriptCompilationConfiguration,
            ScriptEvaluationConfiguration {
                constructorArgs(context)
            }
        )
    }
}