package org.soyuz.kcraft.computer.api.minecraft

data class KCraftPosition(
    val x: Int,
    val y: Int,
    val z: Int
) {
    fun offset(
        dx: Int = 0,
        dy: Int = 0,
        dz: Int = 0
    ) = KCraftPosition(
        x + dx,
        y + dy,
        z + dz
    )
}