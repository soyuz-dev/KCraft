package org.soyuz.kcraft.computer

import org.soyuz.kcraft.computer.api.KCraftEnvironment
import org.soyuz.kcraft.computer.api.KCraftScriptContext
import org.soyuz.kcraft.computer.api.KCraftTerminal
import org.soyuz.kcraft.computer.api.KCraftFileSystem

internal class RuntimeScriptContext(
    runtime: ComputerRuntime
) : KCraftScriptContext {

    override val terminal: KCraftTerminal =
        RuntimeScriptTerminal(runtime.terminal)

    override val files: KCraftFileSystem =
        RuntimeScriptFileSystem(runtime)

    override val env: KCraftEnvironment =
        RuntimeScriptEnvironment(runtime)

}

class RuntimeScriptTerminal(
    private val terminal: Terminal
) : KCraftTerminal {

    override fun println(text: String) {
        terminal.appendLine(text)
    }
}

internal class RuntimeScriptFileSystem(
    private val runtime: ComputerRuntime
) : KCraftFileSystem {

    private val fileSystem
        get() = runtime.fileSystem

    private fun resolve(path: String): String =
        fileSystem.normalizePath(
            runtime.workingDirectory,
            path
        )

    override fun read(path: String): String =
        fileSystem.readFile(
            resolve(path)
        )

    override fun write(
        path: String,
        content: String
    ) {
        fileSystem.writeFile(
            resolve(path),
            content
        )
    }

    override fun append(
        path: String,
        content: String
    ) {
        fileSystem.appendFile(
            resolve(path),
            content
        )
    }

    override fun exists(path: String): Boolean =
        fileSystem.exists(
            resolve(path)
        )

    override fun isDirectory(path: String): Boolean =
        fileSystem.isDirectory(
            resolve(path)
        )

    override fun list(path: String): List<String> =
        fileSystem.list(
            resolve(path)
        )

    override fun createFile(path: String) {
        fileSystem.createFile(
            resolve(path)
        )
    }

    override fun createDirectory(path: String) {
        fileSystem.createDirectory(
            resolve(path)
        )
    }

    override fun deleteFile(path: String) {
        fileSystem.deleteFile(
            resolve(path)
        )
    }

    override fun deleteDirectory(path: String) {
        fileSystem.deleteDirectory(
            resolve(path)
        )
    }
}

internal class RuntimeScriptEnvironment(
    private val runtime: ComputerRuntime
) : KCraftEnvironment {

    override fun get(name: String): String? =
        runtime.environment.get(name)
}
