package org.soyuz.kcraft.computer.api

interface KCraftEnvironment {
    operator fun get(name: String): String?
}