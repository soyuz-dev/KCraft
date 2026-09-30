package org.soyuz.kcraft

import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.world.item.CreativeModeTabs
import net.minecraft.world.item.Item
import org.soyuz.kcraft.golem.RubyGolemChassisItem
import org.soyuz.kcraft.util.key

object KCraftItems {

    fun <T : Item> register(
        name: String,
        itemFactory: (Item.Properties) -> T,
        settings: Item.Properties
    ): T {
        // Create the item key.
        val itemKey = Registries.ITEM.key(name)
        // Create the item instance.
        val item = itemFactory(settings.setId(itemKey))

        // Register the item.
        Registry.register(BuiltInRegistries.ITEM, itemKey, item)

        return item
    }

    val RUBY = register(
        "ruby",
        ::Item,
        Item.Properties()
    )

    val COMPUTER_CHIP = register(
        "computer_chip",
        ::Item,
        Item.Properties()
    )

    val RUBY_GOLEM_CHASSIS =
        register(
            "ruby_golem_chassis",
            ::RubyGolemChassisItem,
            Item.Properties()
                .stacksTo(1)
        )

    val RUBY_GOLEM =
        register(
            "ruby_golem",
            ::Item,
            Item.Properties()
                .stacksTo(1)
        )

    fun initialize() {
        CreativeModeTabEvents
            .modifyOutputEvent(
                CreativeModeTabs.INGREDIENTS
            )
            .register(
                CreativeModeTabEvents.ModifyOutput {
                    it.accept(RUBY)
                    it.accept(COMPUTER_CHIP)
                    it.accept(RUBY_GOLEM_CHASSIS)
                    it.accept(RUBY_GOLEM)
                }
            )
    }

}