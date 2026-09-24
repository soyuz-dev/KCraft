package org.soyuz.kcraft.computer.api.minecraft

interface KCraftWorld {

    fun blockAt(
        x: Int,
        y: Int,
        z: Int
    ): KCraftBlock
}