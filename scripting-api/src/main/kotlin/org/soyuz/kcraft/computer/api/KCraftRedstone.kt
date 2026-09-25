package org.soyuz.kcraft.computer.api

import org.soyuz.kcraft.computer.api.minecraft.KCraftDirection

interface KCraftRedstone {
    fun read(direction: KCraftDirection): Int

    fun write(
        direction: KCraftDirection,
        strength: Int
    )

    fun clear() {
        KCraftDirection.entries.forEach {
            write(it, 0)
        }
    }
}