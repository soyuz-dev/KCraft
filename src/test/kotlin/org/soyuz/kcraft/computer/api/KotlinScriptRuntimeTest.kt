package org.soyuz.kcraft.computer.api

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.soyuz.kcraft.computer.scripting.KotlinScriptRuntime
import kotlin.script.experimental.api.ResultWithDiagnostics

class KotlinScriptRuntimeTest {

    @Test
    fun `script can print to KCraft terminal`() {
        val output = mutableListOf<String>()

        val terminal = object : KCraftTerminal {
            override fun println(text: String) {
                output += text
            }
        }

        val context = object : KCraftScriptContext {
            override val terminal: KCraftTerminal =
                terminal
        }

        val runtime =
            KotlinScriptRuntime(context)

        val result = runtime.execute(
            """
            terminal.println("Hello from Kotlin!")
            """.trimIndent()
        )

        assertTrue(
            result is ResultWithDiagnostics.Success,
            result.reports.joinToString("\n") {
                it.message
            }
        )

        assertEquals(
            listOf("Hello from Kotlin!"),
            output
        )
    }

    @Test
    fun `invalid script returns diagnostics`() {
        val terminal = object : KCraftTerminal {
            override fun println(text: String) = Unit
        }

        val context = object : KCraftScriptContext {
            override val terminal = terminal
        }

        val runtime =
            KotlinScriptRuntime(context)

        val result = runtime.execute(
            """
        terminal.thisFunctionDoesNotExist()
        """.trimIndent()
        )

        assertTrue(
            result is ResultWithDiagnostics.Failure
        )

        assertTrue(
            result.reports.any {
                it.message.contains(
                    "unresolved reference",
                    ignoreCase = true
                )
            }
        )
    }
}