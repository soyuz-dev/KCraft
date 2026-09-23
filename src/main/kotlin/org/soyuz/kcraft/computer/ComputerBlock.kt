package org.soyuz.kcraft.computer

import com.mojang.serialization.MapCodec
import net.minecraft.core.BlockPos
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockBehaviour.Properties
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult
import org.soyuz.kcraft.KCraftBlockEntities

class ComputerBlock(properties: Properties) : BaseEntityBlock(properties) {

    override fun newBlockEntity(
        worldPosition: BlockPos,
        blockState: BlockState
    ): BlockEntity = ComputerBlockEntity(worldPosition, blockState)

    override fun codec(): MapCodec<out BaseEntityBlock> = CODEC

    companion object {
        val CODEC: MapCodec<ComputerBlock> =
            simpleCodec(::ComputerBlock)
    }

    override fun useWithoutItem(
        state: BlockState,
        level: Level,
        pos: BlockPos,
        player: Player,
        hitResult: BlockHitResult
    ): InteractionResult {
        println("ComputerBlock used; client=${level.isClientSide}")

        if (!level.isClientSide) {
            val computer =
                level.getBlockEntity(pos) as? ComputerBlockEntity

            println("Block entity = $computer")

            if (computer != null) {
                println("Opening menu")
                player.openMenu(computer)
            }
        }

        return InteractionResult.SUCCESS
    }

    override fun <T : BlockEntity> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T>
    ): BlockEntityTicker<T>? {
        if (level.isClientSide) {
            return null
        }

        return createTickerHelper(
            blockEntityType,
            KCraftBlockEntities.COMPUTER
        ) { _, _, _, blockEntity ->
            blockEntity.serverTick()
        }
    }


}