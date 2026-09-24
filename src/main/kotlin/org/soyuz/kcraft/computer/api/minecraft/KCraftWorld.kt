package org.soyuz.kcraft.computer.api.minecraft

interface KCraftWorld {

    fun blockAt(position: KCraftPosition): KCraftBlock

    fun blockAt(
        x: Int,
        y: Int,
        z: Int
    ): KCraftBlock = blockAt(KCraftPosition(x, y, z))
}