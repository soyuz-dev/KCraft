package org.soyuz.kcraft.computer

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntitySpawnReason
import org.soyuz.kcraft.KCraftEntities
import org.soyuz.kcraft.computer.scripting.KotlinScriptRuntime
import org.soyuz.kcraft.computer.api.minecraft.KCraftBlock
import org.soyuz.kcraft.computer.api.minecraft.KCraftDirection
import org.soyuz.kcraft.computer.api.minecraft.KCraftPosition
import org.soyuz.kcraft.computer.ksh.KShEnvironment
import org.soyuz.kcraft.computer.ksh.KShInterpreter
import org.soyuz.kcraft.computer.process.ComputerInfoRequest
import org.soyuz.kcraft.computer.process.ComputerRequest
import org.soyuz.kcraft.computer.process.FileRequest
import org.soyuz.kcraft.computer.process.KCraftProcessManager
import org.soyuz.kcraft.computer.process.RedstoneRequest
import org.soyuz.kcraft.computer.process.WorldRequest
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentLinkedQueue

class ComputerRuntime(
    val terminal: Terminal,
    val fileSystem: FileSystem,
    internal val level: ServerLevel,
    internal val position: BlockPos,
) {

    val id: UUID =
        computerId(
            level,
            position
        )


    companion object {
        private const val MAX_REQUESTS_PER_TICK = 128
    }


    var activeMode: ComputerMode = terminal
        private set

    var workingDirectory: String = "/"
        private set

    var uptimeTicks = 0L
        private set

    private var booted = false

    val environment = KShEnvironment(this)
    val interpreter = KShInterpreter(this)
    val kotlin = KotlinScriptRuntime()
    val processes = KCraftProcessManager(this)
    private val requests =
        ConcurrentLinkedQueue<ComputerRequest>()

    fun submitRequest(request: ComputerRequest) {
        requests.add(request)
    }

    fun tick(): Boolean{
        if (!booted) return false
        uptimeTicks++

        return processRequests()
    }

    fun boot() {
        if (booted) return

        booted = true

        if (fileSystem.exists("/etc/shell.kshrc")) {
            interpreter.executeLine(
                "source /etc/shell.kshrc"
            )
        }
    }

    fun handleInput(input: ComputerInput) {
        activeMode.handleInput(
            input,
            this
        )
    }

    fun displayState(): ComputerDisplayState =
        activeMode.displayState()

    fun submitCurrentCommand() {
        val command = terminal
            .commitInput()
            .trim()

        if (command.isNotEmpty()) {
            interpreter.executeLine(command)
        }
    }

    fun changeDirectory(path: String): Boolean {
        val normalized = fileSystem.normalizePath(
            workingDirectory,
            path
        )

        if (!fileSystem.exists(normalized)) return false
        if (!fileSystem.isDirectory(normalized)) return false

        workingDirectory = normalized
        return true
    }

    fun openPico(path: String) {
        val normalized = fileSystem.normalizePath(
            workingDirectory,
            path
        )

        if (
            fileSystem.exists(normalized) &&
            fileSystem.isDirectory(normalized)
        ) {
            terminal.appendLine(
                "pico: is a directory: $path"
            )
            return
        }

        val contents =
            if (fileSystem.exists(normalized)) {
                fileSystem.readFile(normalized)
            } else {
                ""
            }

        activeMode = PicoMode(
            path = normalized,
            contents = contents
        )
    }

    fun returnToTerminal() {
        activeMode = terminal
    }

    private fun processRequests(): Boolean {
        var changed = false

        repeat(MAX_REQUESTS_PER_TICK) {
            val request =
                requests.poll()
                    ?: return changed

            when (request) {
                is ComputerRequest.TerminalOutput -> {
                    terminal.appendLine(request.text)
                    changed = true
                }

                is FileRequest ->
                    processFileRequest(request)

                is WorldRequest ->
                    processWorldRequest(request)

                is ComputerInfoRequest ->
                    processComputerInfoRequest(request)

                is RedstoneRequest ->
                    processRedstoneRequest(request)
            }
        }

        return changed
    }

    private fun processFileRequest(request: FileRequest) {
        when(request) {
            is FileRequest.WriteFile -> {
                complete(request.result, { fileSystem.writeFile(request.path, request.content) })
            }

            is FileRequest.ReadFile -> {
                complete(request.result, { fileSystem.readFile(request.path) })
            }

            is FileRequest.FileExists -> {
                complete(request.result, { fileSystem.exists(request.path) })
            }

            is FileRequest.AppendFile -> {
                complete(request.result, { fileSystem.appendFile(request.path, request.content) })
            }

            is FileRequest.CreateFile -> {
                complete(request.result, { fileSystem.createFile(request.path) })
            }

            is FileRequest.DeleteFile -> {
                complete(request.result, { fileSystem.deleteFile(request.path) })
            }

            is FileRequest.IsDirectory -> {
                complete(request.result, { fileSystem.isDirectory(request.path) })
            }

            is FileRequest.ListDirectory -> {
                complete(request.result, { fileSystem.list(request.path) })
            }

            is FileRequest.CreateDirectory -> {
                complete(request.result, { fileSystem.createDirectory(request.path) })
            }

            is FileRequest.DeleteDirectory -> {
                complete(request.result, { fileSystem.deleteDirectory(request.path) })
            }
        }
    }

    private fun processWorldRequest(request: WorldRequest) {
        when (request) {
            is WorldRequest.GetBlock -> {
                complete(request.result) {
                    val state =
                        level.getBlockState(
                            BlockPos(
                                request.x,
                                request.y,
                                request.z
                            )
                        )

                    KCraftBlock(
                        id = BuiltInRegistries.BLOCK
                            .getKey(state.block)
                            .toString()
                    )
                }
            }

        }
    }

    private fun processComputerInfoRequest(request: ComputerInfoRequest) {
        when (request) {
            is ComputerInfoRequest.GetSelfPosition -> {
                complete(request.result) {
                    KCraftPosition(
                        position.x,
                        position.y,
                        position.z
                    )
                }
            }
        }
    }
    private inline fun <T> complete(
        future: CompletableFuture<T>,
        operation: () -> T
    ) {
        try {
            future.complete(
                operation()
            )
        } catch (e: Throwable) {
            future.completeExceptionally(e)
        }
    }

    private fun processRedstoneRequest(
        request: RedstoneRequest
    ) {
        when (request) {
            is RedstoneRequest.Write -> {
                complete(request.result) {
                    val blockEntity =
                        level.getBlockEntity(position)
                                as? ComputerBlockEntity
                            ?: error(
                                "Computer block entity is unavailable"
                            )

                    blockEntity.setRedstoneOutput(
                        request.direction,
                        request.strength
                    )
                }
            }

            is RedstoneRequest.Read -> {
                complete(request.result) {
                    readRedstoneInput(
                        request.direction
                    )
                }
            }
        }
    }

    private fun readRedstoneInput(
        direction: KCraftDirection
    ): Int {
        val minecraftDirection =
            direction.toMinecraft()

        val neighbourPos =
            position.relative(minecraftDirection)

        val neighbourState =
            level.getBlockState(neighbourPos)

        return neighbourState.getSignal(
            level,
            neighbourPos,
            minecraftDirection
        )
    }

    fun summonRubyGolem(): Boolean {
        val golem =
            KCraftEntities.RUBY_GOLEM.create(
                level,
                EntitySpawnReason.MOB_SUMMONED
            ) ?: return false

        golem.bindToComputer(
            computerId = id
        )

        golem.setPos(
            position.x + 0.5,
            position.y + 1.0,
            position.z + 0.5
        )

        return level.addFreshEntity(golem)
    }

    private fun computerId(
        level: ServerLevel,
        position: BlockPos
    ): UUID {
        val identity =
            "${level.dimension().identifier()}:${position.x},${position.y},${position.z}"

        return UUID.nameUUIDFromBytes(
            identity.toByteArray(
                Charsets.UTF_8
            )
        )
    }


    internal fun KCraftDirection.toMinecraft(): Direction =
        when (this) {
            KCraftDirection.NORTH -> Direction.NORTH
            KCraftDirection.SOUTH -> Direction.SOUTH
            KCraftDirection.EAST -> Direction.EAST
            KCraftDirection.WEST -> Direction.WEST
        }
}

