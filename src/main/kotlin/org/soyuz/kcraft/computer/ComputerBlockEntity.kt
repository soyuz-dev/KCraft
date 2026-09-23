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
import org.soyuz.kcraft.network.s2c.ComputerDisplayStatePayload
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

    fun isViewer(player: ServerPlayer) =
        viewers.contains(player)


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

    fun syncDisplay(player: ServerPlayer) {
        val state = runtime.displayState()

        ServerPlayNetworking.send(
            player,
            ComputerDisplayStatePayload(
                lines = state.lines,
                cursorRow = state.cursorRow ?: -1,
                cursorColumn = state.cursorColumn ?: -1
            )
        )
    }

    fun syncDisplay() {
        viewers.forEach(::syncDisplay)
    }

    fun serverTick() {
        val runtime = _runtime ?: return

        if (runtime.tick()) {
            syncDisplay()
        }
    }
}