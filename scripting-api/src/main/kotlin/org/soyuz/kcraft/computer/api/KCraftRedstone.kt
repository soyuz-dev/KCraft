package org.soyuz.kcraft.computer.api

import org.soyuz.kcraft.computer.api.minecraft.KCraftDirection

interface KCraftRedstone {
    fun read(direction: KCraftDirection): Int

    fun write(
        direction: KCraftDirection,
        strength: Int
    )
}