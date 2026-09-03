package org.soyuz.kcraft

import net.fabricmc.fabric.api.`object`.builder.v1.block.entity.FabricBlockEntityTypeBuilder
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityType
import org.soyuz.kcraft.computer.ComputerBlockEntity
import org.soyuz.kcraft.util.key

object KCraftBlockEntities {

    private fun <T : BlockEntity> register(
        name: String,
        entityFactory: FabricBlockEntityTypeBuilder.Factory<T>,
        vararg blocks: Block
    ): BlockEntityType<T> {
        val key = Registries.BLOCK_ENTITY_TYPE.key(name)
        return Registry.register(
            BuiltInRegistries.BLOCK_ENTITY_TYPE,
            key,
            FabricBlockEntityTypeBuilder.create(entityFactory, *blocks).build()
        )
    }

    val COMPUTER = register(
        "computer_block",
        ::ComputerBlockEntity,
        KCraftBlocks.COMPUTER_BLOCK
    )
    fun initialize() {}

}