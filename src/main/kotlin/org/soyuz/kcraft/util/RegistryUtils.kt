package org.soyuz.kcraft.util

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import org.soyuz.kcraft.KCraft

fun <T : Any> ResourceKey<out Registry<T>>.key(name: String): ResourceKey<T> =
    ResourceKey.create(this, KCraft.id(name))