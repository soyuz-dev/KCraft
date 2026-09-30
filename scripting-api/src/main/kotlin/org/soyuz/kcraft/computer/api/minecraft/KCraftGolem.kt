package org.soyuz.kcraft.computer.api.minecraft

interface KCraftGolem {
    val id: String
    val position: KCraftPosition

    fun moveTo(
        position: KCraftPosition
    )
}