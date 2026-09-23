package org.soyuz.kcraft.computer.api

import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptEvaluationConfiguration
import kotlin.script.experimental.api.constructorArgs
import kotlin.script.experimental.host.toScriptSource
import kotlin.script.experimental.jvmhost.BasicJvmScriptingHost

class KotlinScriptRuntime {

    private val host =
        BasicJvmScriptingHost()

    fun execute(
        source: String,
        name: String,
        context: KCraftScriptContext
    ): ResultWithDiagnostics<EvaluationResult> =
        host.eval(
            source.toScriptSource(name),
            KCraftScriptCompilationConfiguration,
            ScriptEvaluationConfiguration {
                constructorArgs(context)
            }
        )
}