package org.soyuz.kcraft.computer.scripting

import org.soyuz.kcraft.computer.api.KCraftScript
import java.io.File
import kotlin.script.experimental.api.ScriptCompilationConfiguration
import kotlin.script.experimental.api.baseClass
import kotlin.script.experimental.api.defaultImports
import kotlin.script.experimental.api.dependencies
import kotlin.script.experimental.jvm.JvmDependency
import kotlin.script.experimental.jvm.jvm

object KCraftScriptCompilationConfiguration :
    ScriptCompilationConfiguration({

        baseClass(KCraftScript::class)

        defaultImports(
            "org.soyuz.kcraft.computer.api.*",
            "org.soyuz.kcraft.computer.api.minecraft.*"
        )


        jvm {
            dependencies(
                JvmDependency(
                    listOf(
                        classpathOf(KCraftScript::class.java),
                        classpathOf(Unit::class.java)
                    ).distinct()
                )
            )
        }
    }) {
    @Suppress("unused")
    private fun readResolve(): Any =
        KCraftScriptCompilationConfiguration
}

private fun classpathOf(
    aClass: Class<*>
): File {
    val location =
        aClass.protectionDomain
            .codeSource
            ?.location
            ?: error(
                "Cannot determine classpath for ${aClass.name}"
            )

    return File(location.toURI())
}