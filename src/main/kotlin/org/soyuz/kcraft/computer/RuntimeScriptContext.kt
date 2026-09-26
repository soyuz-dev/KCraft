package org.soyuz.kcraft.computer

import org.soyuz.kcraft.computer.api.KCraftComputer
import org.soyuz.kcraft.computer.api.KCraftEnvironment
import org.soyuz.kcraft.computer.api.KCraftFileSystem
import org.soyuz.kcraft.computer.api.KCraftRedstone
import org.soyuz.kcraft.computer.api.KCraftScriptContext
import org.soyuz.kcraft.computer.api.KCraftTerminal
import org.soyuz.kcraft.computer.api.minecraft.KCraftBlock
import org.soyuz.kcraft.computer.api.minecraft.KCraftDirection
import org.soyuz.kcraft.computer.api.minecraft.KCraftGolem
import org.soyuz.kcraft.computer.api.minecraft.KCraftPosition
import org.soyuz.kcraft.computer.api.minecraft.KCraftWorld
import org.soyuz.kcraft.computer.process.ComputerInfoRequest
import org.soyuz.kcraft.computer.process.ComputerRequest
import org.soyuz.kcraft.computer.process.FileRequest
import org.soyuz.kcraft.computer.process.GolemRequest
import org.soyuz.kcraft.computer.process.KCraftProcess
import org.soyuz.kcraft.computer.process.RedstoneRequest
import org.soyuz.kcraft.computer.process.WorldRequest
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionException

internal class RuntimeScriptContext(
    runtime: ComputerRuntime,
    process: KCraftProcess
) : KCraftScriptContext {

    override val terminal: KCraftTerminal =
        RuntimeScriptTerminal(runtime)

    override val files: KCraftFileSystem =
        RuntimeScriptFileSystem(runtime, process.workingDirectory)

    override val env: KCraftEnvironment =
        RuntimeScriptEnvironment(process)

    override val world: KCraftWorld =
        RuntimeScriptWorld(runtime)

    override val computer: KCraftComputer =
        RuntimeScriptComputer(runtime)

    override val redstone: KCraftRedstone =
        RuntimeScriptRedstone(runtime)
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
    private val runtime: ComputerRuntime,
    private val workingDirectory: String
) : KCraftFileSystem {

    private fun resolve(path: String): String =
        runtime.fileSystem.normalizePath(
            workingDirectory,
            path
        )

    override fun read(path: String): String {
        val result = CompletableFuture<String>()

        runtime.submitRequest(
            FileRequest.ReadFile(
                resolve(path),
                result
            )
        )

        return result.await()
    }

    override fun write(
        path: String,
        content: String
    ) {
        val result = CompletableFuture<Unit>()

        runtime.submitRequest(
            FileRequest.WriteFile(
                path = resolve(path),
                content = content,
                result = result
            )
        )

        result.await()
    }

    override fun append(
        path: String,
        content: String
    ) {
        val result = CompletableFuture<Unit>()

        runtime.submitRequest(
            FileRequest.AppendFile(
                path = resolve(path),
                content = content,
                result = result
            )
        )

        result.await()
    }

    override fun exists(path: String): Boolean {
        val result = CompletableFuture<Boolean>()
        runtime.submitRequest(
            FileRequest.FileExists(
                resolve(path),
                result
            )
        )
        return result.await()
    }

    override fun isDirectory(path: String): Boolean {
        val result = CompletableFuture<Boolean>()
        runtime.submitRequest(
            FileRequest.IsDirectory(
                resolve(path),
                result
            )
        )
        return result.await()
    }

    override fun list(path: String): List<String> {
        val result = CompletableFuture<List<String>>()
        runtime.submitRequest(
            FileRequest.ListDirectory(
                resolve(path),
                result
            )
        )
        return result.await()
    }

    override fun createFile(path: String) {
        val result = CompletableFuture<Unit>()
        runtime.submitRequest(
            FileRequest.CreateFile(
                resolve(path),
                result
            )
        )
        return result.await()
    }

    override fun createDirectory(path: String) {
        val result = CompletableFuture<Unit>()
        runtime.submitRequest(
            FileRequest.CreateDirectory(
                resolve(path),
                result
            )
        )
        return result.await()
    }

    override fun deleteFile(path: String) {
        val result = CompletableFuture<Unit>()
        runtime.submitRequest(
            FileRequest.DeleteFile(
                resolve(path),
                result
            )
        )
        return result.await()
    }

    override fun deleteDirectory(path: String) {
        val result = CompletableFuture<Unit>()
        runtime.submitRequest(
            FileRequest.DeleteDirectory(
                resolve(path),
                result
            )
        )
        return result.await()
    }
}

internal class RuntimeScriptEnvironment(
    private val process: KCraftProcess
) : KCraftEnvironment {

    override fun get(name: String): String? =
        when (name) {
            "PWD" ->
                process.workingDirectory
            "PID" ->
                process.pid.toString()
            else ->
                process.environment[name]
        }
}


internal class RuntimeScriptWorld(
    private val runtime: ComputerRuntime
) : KCraftWorld {

    override fun blockAt(
        position: KCraftPosition
    ): KCraftBlock {
        val result =
            CompletableFuture<KCraftBlock>()

        runtime.submitRequest(
            WorldRequest.GetBlock(
                x = position.x,
                y = position.y,
                z = position.z,
                result = result
            )
        )

        return result.await()
    }
}

internal class RuntimeScriptComputer(
    private val runtime: ComputerRuntime
): KCraftComputer {

    override val golems: List<KCraftGolem>
        get() {
            val result =
                CompletableFuture<List<KCraftGolem>>()

            runtime.submitRequest(
                GolemRequest.GetOwnedGolems(
                    result
                )
            )

            return result.await()
        }

    override val position: KCraftPosition
        get() {
            val result =
                CompletableFuture<KCraftPosition>()

            runtime.submitRequest(
                ComputerInfoRequest.GetSelfPosition(
                    result = result
                )
            )

            return result.await()
        }
}

internal class RuntimeScriptRedstone(
    private val runtime: ComputerRuntime
) : KCraftRedstone {

    override fun read(
        direction: KCraftDirection
    ): Int {
        val result =
            CompletableFuture<Int>()

        runtime.submitRequest(
            RedstoneRequest.Read(
                direction,
                result
            )
        )

        return result.await()
    }

    override fun write(
        direction: KCraftDirection,
        strength: Int
    ) {
        require(strength in 0..15) {
            "Redstone strength must be between 0 and 15"
        }

        val result =
            CompletableFuture<Unit>()

        runtime.submitRequest(
            RedstoneRequest.Write(
                direction,
                strength,
                result
            )
        )

        result.await()
    }



}

private fun <T> CompletableFuture<T>.await(): T =
    try {
        join()
    } catch (e: CompletionException) {
        throw e.cause ?: e
    }