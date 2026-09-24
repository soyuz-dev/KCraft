package org.soyuz.kcraft.computer.process

import java.util.concurrent.Future
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics

class KCraftProcess(
    val pid: Int,
    val path: String,
    val workingDirectory: String,
    val environment: Map<String, String>
) {
    @Volatile
    var state: KCraftProcessState =
        KCraftProcessState.STARTING
        internal set

    @Volatile
    var result: ResultWithDiagnostics<EvaluationResult>? = null
        internal set

    @Volatile
    var failure: String? = null
        internal set

    @Volatile
    internal var future: Future<*>? = null
}