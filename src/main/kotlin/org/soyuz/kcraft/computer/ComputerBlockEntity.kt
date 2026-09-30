package org.soyuz.kcraft.computer

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.SimpleContainer
import net.minecraft.world.entity.EntitySpawnReason
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.LevelResource
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.soyuz.kcraft.KCraftBlockEntities
import org.soyuz.kcraft.KCraftEntities
import org.soyuz.kcraft.KCraftItems
import org.soyuz.kcraft.computer.api.minecraft.KCraftDirection
import org.soyuz.kcraft.network.s2c.ComputerDisplayStatePayload
import java.util.*

class ComputerBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(
    KCraftBlockEntities.COMPUTER,
    pos,
    state
), MenuProvider {

    val golemSlot =
        object : SimpleContainer(1) {
            override fun setChanged() {
                super.setChanged()

                this@ComputerBlockEntity
                    .setChanged()
            }
        }


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

    private val redstoneOutputs =
        mutableMapOf(
            KCraftDirection.NORTH to 0,
            KCraftDirection.SOUTH to 0,
            KCraftDirection.EAST to 0,
            KCraftDirection.WEST to 0
        )

    fun getRedstoneOutput(
        direction: KCraftDirection
    ): Int =
        redstoneOutputs.getValue(direction)

    fun setRedstoneOutput(
        direction: KCraftDirection,
        strength: Int
    ) {
        require(strength in 0..15)

        if (redstoneOutputs[direction] == strength) {
            return
        }

        redstoneOutputs[direction] = strength

        setChanged()

        level?.updateNeighborsAt(
            blockPos,
            blockState.block
        )
    }

    val runtime: ComputerRuntime
        get() = _runtime
            ?: error("Computer runtime has not been initialised")

    override fun saveAdditional(
        output: ValueOutput
    ) {
        super.saveAdditional(output)

        output.putString(
            "computer_id",
            computerId.toString()
        )

        golemSlot.storeAsItemList(
            output.list(
                "golem",
                ItemStack.CODEC
            )
        )
    }


    override fun loadAdditional(
        input: ValueInput
    ) {
        super.loadAdditional(input)

        val saved =
            input.getStringOr(
                "computer_id",
                ""
            )

        computerId =
            runCatching {
                UUID.fromString(saved)
            }.getOrElse {
                UUID.randomUUID().also {
                    setChanged()
                }
            }

        golemSlot.fromItemList(
            input.listOrEmpty(
                "golem",
                ItemStack.CODEC
            )
        )
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
                computerId,
            ),
            serverLevel,
            blockPos,
            computerId
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


    fun deployGolem(): Boolean {
        val serverLevel =
            level as? ServerLevel
                ?: return false

        /*
         * The slot is authoritative.
         *
         * If there is no Ruby Golem item in it, there is
         * nothing to deploy.
         */
        val stack =
            golemSlot.getItem(0)

        if (
            stack.isEmpty ||
            !stack.`is`(KCraftItems.RUBY_GOLEM)
        ) {
            return false
        }


        /*
         * Don't allow this computer to deploy a second golem
         * while it already owns one in the world.
         */
        val alreadyDeployed =
            serverLevel.getEntities(
                KCraftEntities.RUBY_GOLEM
            ) { golem ->
                golem.parentComputerId ==
                        computerId
            }
                .isNotEmpty()

        if (alreadyDeployed) {
            return false
        }


        /*
         * Create the entity, but don't remove the item yet.
         *
         * Until addFreshEntity() succeeds, the ItemStack in
         * the computer remains the authoritative Gerald.
         */
        val golem =
            KCraftEntities.RUBY_GOLEM.create(
                serverLevel,
                EntitySpawnReason.MOB_SUMMONED
            )
                ?: return false


        /*
         * Restore Gerald's name from the Ruby Golem item.
         *
         * The item got this CUSTOM_NAME component from the
         * original Allay when the chassis captured it.
         */
        stack.get(
            DataComponents.CUSTOM_NAME
        )?.let { name ->
            golem.customName =
                name
        }


        /*
         * Associate this deployed entity with this computer.
         */
        golem.bindToComputer(
            computerId
        )


        /*
         * Temporary deployment position:
         *
         * directly above the centre of the computer.
         *
         * We'll make spawn-position selection smarter later.
         */
        golem.setPos(
            blockPos.x + 0.5,
            blockPos.y + 1.0,
            blockPos.z + 0.5
        )


        /*
         * IMPORTANT:
         *
         * Only consume the item AFTER Minecraft has accepted
         * the entity into the world.
         */
        if (
            !serverLevel.addFreshEntity(
                golem
            )
        ) {
            return false
        }


        /*
         * Gerald now exists as an entity, so remove the item
         * representation from the computer.
         *
         * removeItem() also calls our SimpleContainer's
         * setChanged(), which marks this block entity dirty.
         */
        golemSlot.removeItem(
            0,
            1
        )

        return true
    }

}

