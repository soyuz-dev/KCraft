package org.soyuz.kcraft

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.SoundType
import net.minecraft.world.level.block.state.BlockBehaviour
import org.soyuz.kcraft.computer.ComputerBlock
import org.soyuz.kcraft.util.key

object KCraftBlocks {

    fun<T: Block> register (
        name: String,
        blockFactory: (BlockBehaviour.Properties) -> T,
        properties: BlockBehaviour.Properties,
        shouldRegisterItem: Boolean = true
    ) : T {

        val blockKey = Registries.BLOCK.key(name)

        val block = blockFactory(properties.setId(blockKey))

        if ( shouldRegisterItem ) {
            val itemKey = Registries.ITEM.key(name)

            val blockItem = BlockItem(
                block,
                Item.Properties()
                    .setId(itemKey)
                    .useBlockDescriptionPrefix()
            )

            Registry.register(BuiltInRegistries.ITEM, itemKey, blockItem)
        }

        return Registry.register(BuiltInRegistries.BLOCK, blockKey, block)
    }

    val RUBY_BLOCK = register(
        "ruby_block",
        ::Block,
        BlockBehaviour.Properties.of().sound(SoundType.IRON)
    )

    val COMPUTER_BLOCK = register(
        "computer_block",
        ::ComputerBlock,
        BlockBehaviour.Properties.of()
    )


    fun initialize() {
        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.BUILDING_BLOCKS)
            .register(CreativeModeTabEvents.ModifyOutput {
                it.accept(RUBY_BLOCK.asItem())
            })
    }

}