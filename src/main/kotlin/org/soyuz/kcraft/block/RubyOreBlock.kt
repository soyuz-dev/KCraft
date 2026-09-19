package org.soyuz.kcraft.block

import net.minecraft.util.valueproviders.UniformInt
import net.minecraft.world.level.block.DropExperienceBlock
import net.minecraft.world.level.block.state.BlockBehaviour

class RubyOreBlock(
    properties: BlockBehaviour.Properties
): DropExperienceBlock(
    UniformInt.of(2,5),
    properties
)