package org.soyuz.kcraft.computer

import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.state.BlockState
import org.soyuz.kcraft.KCraftBlockEntities

class ComputerBlockEntity(
    pos: BlockPos,
    state: BlockState
) : BlockEntity(
    KCraftBlockEntities.COMPUTER,
    pos,
    state
)