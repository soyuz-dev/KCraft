package org.soyuz.kcraft.computer

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.core.BlockPos
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.soyuz.kcraft.KCraftBlockEntities
import org.soyuz.kcraft.network.c2s.TerminalInputPayload
import org.soyuz.kcraft.network.s2c.TerminalStatePayload
import java.util.UUID

class ComputerBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(
    KCraftBlockEntities.COMPUTER,
    pos,
    state
), MenuProvider {

    private val viewers = mutableSetOf<ServerPlayer>()

    fun addViewer(player: ServerPlayer) =
        viewers.add(player)

    fun removeViewer(player: ServerPlayer) =
        viewers.remove(player)


    var computerId: UUID = UUID.randomUUID()
        private set

    private var _runtime: ComputerRuntime? = null

    val runtime: ComputerRuntime
        get() = _runtime
            ?: error("Computer runtime has not been initialised")

    override fun saveAdditional(output: ValueOutput) {
        super.saveAdditional(output)

        output.putString(
            "computer_id",
            computerId.toString()
        )
    }

    override fun loadAdditional(input: ValueInput) {
        super.loadAdditional(input)

        val saved = input.getStringOr(
            "computer_id",
            ""
        )

        computerId = runCatching {
            UUID.fromString(saved)
        }.getOrElse {
            UUID.randomUUID().also {
                setChanged()
            }
        }
    }

    private fun ensureRuntime() {
        if (_runtime != null) return

        val serverLevel = level as? ServerLevel
            ?: error("Cannot initialise computer runtime without a ServerLevel")

        val worldRoot = serverLevel.server
            .getWorldPath(LevelResource.ROOT)

        _runtime = ComputerRuntime(
            terminal = Terminal(),
            fileSystem = FileSystem(
                worldRoot,
                computerId
            )
        )
    }

    override fun getDisplayName(): Component =
        Component.translatable(
            "block.kcraft.computer_block"
        )

    override fun createMenu(
        containerId: Int,
        inventory: Inventory,
        player: Player
    ): AbstractContainerMenu {
        println("ComputerBlockEntity.createMenu")

        ensureRuntime()
        runtime.boot()

        if (player is ServerPlayer) {
            viewers += player
        }

        return ComputerMenu(
            containerId,
            inventory,
            this
        )
    }

    fun syncTerminal(player: ServerPlayer) {
        val terminal = runtime.terminal
        val cursor = terminal.visibleCursorPosition

        val payload = TerminalStatePayload(
            terminal.visibleLines.toList(),
            cursor?.row ?: -1,
            cursor?.column ?: -1
        )

        ServerPlayNetworking.send(player, payload)
    }

    fun syncTerminal() {
        viewers.forEach(::syncTerminal)
    }

    fun handleInput(
        player: ServerPlayer,
        payload: TerminalInputPayload
    ) {
        if (player !in viewers) return

        when (payload.type) {
            TerminalInputPayload.Type.CHARACTER -> {
                Character.toChars(payload.character)
                    .concatToString()
                    .forEach(runtime.terminal::appendChar)
            }

            TerminalInputPayload.Type.BACKSPACE ->
                runtime.terminal.popChar()

            TerminalInputPayload.Type.ENTER ->
                runtime.submitCurrentCommand()

            TerminalInputPayload.Type.SCROLL_UP ->
                runtime.terminal.scrollUp()

            TerminalInputPayload.Type.SCROLL_DOWN ->
                runtime.terminal.scrollDown()
        }

        syncTerminal()
    }
}