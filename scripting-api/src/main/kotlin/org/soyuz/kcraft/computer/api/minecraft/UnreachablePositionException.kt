package org.soyuz.kcraft.computer.api.minecraft

class UnreachablePositionException(
    val position: KCraftPosition
) : RuntimeException(
    "Ruby Golem cannot reach $position"
)