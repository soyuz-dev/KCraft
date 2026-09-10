package org.soyuz.kcraft

import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.inventory.MenuType
import org.soyuz.kcraft.computer.ComputerMenu
import org.soyuz.kcraft.util.key

object KCraftMenus {
    val COMPUTER = Registry.register(
        BuiltInRegistries.MENU,
        Registries.MENU.key("computer"),
        MenuType(::ComputerMenu, FeatureFlagSet.of())
    ).also {
        println("Menu added: $it")
    }

    fun initialize() {}
}