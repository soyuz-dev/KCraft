package org.soyuz.kcraft.computer.ksh

import org.soyuz.kcraft.computer.ComputerRuntime

class KShEnvironment(
    private val runtime: ComputerRuntime
) {
    private val variables = mutableMapOf<String, String>()

    init {
        variables["USER"] = "root"
        variables["HOME"] = "/home"
        variables["SHELL"] = "/bin/ksh"
    }

    fun get(name: String): String? =
        when (name) {
            "PWD" -> runtime.workingDirectory
            "UPTIME" -> runtime.uptimeTicks.toString()
            else -> variables[name]
        }

    fun set(name: String, value: String) {
        variables[name] = value
    }

    fun snapshot(): Map<String, String> =
        variables.toMap()
}