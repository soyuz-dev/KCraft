package org.soyuz.kcraft.golem

import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.minecraft.core.component.DataComponents
import net.minecraft.world.InteractionResult
import net.minecraft.world.entity.animal.allay.Allay
import net.minecraft.world.item.ItemStack
import org.soyuz.kcraft.KCraftItems

object RubyGolemInteractions {

    fun initialize() {
        UseEntityCallback.EVENT.register {
                player,
                level,
                hand,
                entity,
                _ ->

            val stack =
                player.getItemInHand(hand)

            if (
                entity !is Allay ||
                !stack.`is`(
                    KCraftItems.RUBY_GOLEM_CHASSIS
                )
            ) {
                return@register InteractionResult.PASS
            }

            /*
             * Fabric calls this callback before vanilla's normal
             * entity interaction. Returning SUCCESS prevents the
             * Allay from treating the chassis as an ordinary item.
             */
            if (level.isClientSide) {
                return@register InteractionResult.SUCCESS
            }

            val golemStack =
                ItemStack(
                    KCraftItems.RUBY_GOLEM
                )

            entity.customName?.let { name ->
                golemStack.set(
                    DataComponents.CUSTOM_NAME,
                    name
                )
            }

            entity.discard()

            stack.shrink(1)

            if (!player.addItem(golemStack)) {
                player.drop(
                    golemStack,
                    false
                )
            }

            InteractionResult.SUCCESS
        }
    }
}