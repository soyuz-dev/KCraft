package org.soyuz.kcraft.computer.process

import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultWithDiagnostics

class KCraftProcess(
    val pid: Int,
    val path: String
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
}