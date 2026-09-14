package org.soyuz.kcraft.computer

data class ComputerDisplayState(
    val lines: List<String>,
    val cursorRow: Int?,
    val cursorColumn: Int?
)