package org.soyuz.kcraft.computer

import org.soyuz.kcraft.computer.api.KCraftEnvironment
import org.soyuz.kcraft.computer.api.KCraftScriptContext
import org.soyuz.kcraft.computer.api.KCraftTerminal
import org.soyuz.kcraft.computer.api.KCraftFileSystem
import org.soyuz.kcraft.computer.process.ComputerRequest
import java.util.concurrent.CompletableFuture

internal class RuntimeScriptContext(
    runtime: ComputerRuntime
) : KCraftScriptContext {

    override val terminal: KCraftTerminal =
        RuntimeScriptTerminal(runtime)

    override val files: KCraftFileSystem =
        RuntimeScriptFileSystem(runtime)

    override val env: KCraftEnvironment =
        RuntimeScriptEnvironment(runtime)

}

internal class RuntimeScriptTerminal(
    private val runtime: ComputerRuntime
) : KCraftTerminal {

    override fun println(text: String) {
        runtime.submitRequest(
            ComputerRequest.TerminalOutput(text)
        )
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
        val result = CompletableFuture<Unit>()

        runtime.submitRequest(
            ComputerRequest.WriteFile(
                path,
                content,
                result
            )
        )

        result.join()
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
