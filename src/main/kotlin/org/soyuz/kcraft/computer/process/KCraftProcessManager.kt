package org.soyuz.kcraft.computer.process

import org.soyuz.kcraft.computer.ComputerRuntime
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicInteger
import kotlin.script.experimental.api.EvaluationResult
import kotlin.script.experimental.api.ResultValue
import kotlin.script.experimental.api.ResultWithDiagnostics
import kotlin.script.experimental.api.ScriptDiagnostic
import kotlin.script.experimental.api.valueOrNull
import kotlin.text.set

class KCraftProcessManager(
    private val runtime: ComputerRuntime
) {
    private val nextPid =
        AtomicInteger(1)

    private val processes =
        ConcurrentHashMap<Int, KCraftProcess>()

    private val executor =
        Executors.newCachedThreadPool()

    fun start(
        path: String,
        source: String
    ): KCraftProcess {
        val process = KCraftProcess(
            pid = nextPid.getAndIncrement(),
            path = path
        )

        processes[process.pid] = process

        executor.submit {
            execute(
                process,
                source
            )
        }

        return process
    }

    fun list(): List<KCraftProcess> =
        processes.values
            .sortedBy(KCraftProcess::pid)


    private fun execute(
        process: KCraftProcess,
        source: String
    ) {
        process.state =
            KCraftProcessState.RUNNING

        try {
            val result =
                runtime.kotlin.execute(
                    source = source,
                    name = process.path
                )

            process.result = result

            reportResult(result)

            process.state =
                if (result.hasFailed()) {
                    KCraftProcessState.FAILED
                } else {
                    KCraftProcessState.FINISHED
                }
        } catch (e: Throwable) {
            val failure = formatException(e)

            process.failure = failure
            process.state =
                KCraftProcessState.FAILED

            runtime.terminal.appendLine(
                "process ${process.pid}: error: $failure"
            )
        }
    }

    private fun ResultWithDiagnostics<EvaluationResult>.hasFailed(): Boolean {
        if (
            reports.any {
                it.severity >= ScriptDiagnostic.Severity.ERROR
            }
        ) {
            return true
        }

        return valueOrNull()
            ?.returnValue is ResultValue.Error
    }

    private fun reportResult(
        result: ResultWithDiagnostics<EvaluationResult>
    ) {
        result.reports
            .filter {
                it.severity >= ScriptDiagnostic.Severity.WARNING
            }
            .forEach { diagnostic ->
                runtime.terminal.appendLine(
                    formatDiagnostic(diagnostic)
                )
            }

        val evaluation =
            result.valueOrNull()
                ?: return

        val returnValue =
            evaluation.returnValue

        if (returnValue is ResultValue.Error) {
            runtime.terminal.appendLine(
                "error: ${formatException(returnValue.error)}"
            )
        }
    }

    private fun formatDiagnostic(
        diagnostic: ScriptDiagnostic
    ): String {
        val location =
            diagnostic.location

        val severity =
            when (diagnostic.severity) {
                ScriptDiagnostic.Severity.FATAL ->
                    "fatal"

                ScriptDiagnostic.Severity.ERROR ->
                    "error"

                ScriptDiagnostic.Severity.WARNING ->
                    "warning"

                ScriptDiagnostic.Severity.INFO ->
                    "info"

                ScriptDiagnostic.Severity.DEBUG ->
                    "debug"
            }

        val message =
            diagnostic.exception
                ?.let(::formatException)
                ?: diagnostic.message

        return if (location != null) {
            "${location.start.line}:${location.start.col}: " +
                    "$severity: $message"
        } else {
            "$severity: $message"
        }
    }

    private fun formatException(
        throwable: Throwable
    ): String {
        val type =
            throwable::class.simpleName
                ?: "Exception"

        val message =
            throwable.message

        return if (message.isNullOrBlank()) {
            type
        } else {
            "$type: $message"
        }
    }
}

