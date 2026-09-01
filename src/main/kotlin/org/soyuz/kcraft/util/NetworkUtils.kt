package org.soyuz.kcraft.util

import net.minecraft.resources.Identifier
import org.soyuz.kcraft.KCraft

fun packet(path: String): Identifier =
    Identifier.fromNamespaceAndPath(KCraft.MOD_ID, path)