package org.soyuz.kcraft

import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.core.registries.Registries
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import net.minecraft.core.Registry
import org.soyuz.kcraft.golem.RubyGolem

object KCraftEntities {

    private val RUBY_GOLEM_KEY =
        ResourceKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(
                "kcraft",
                "ruby_golem"
            )
        )

    val RUBY_GOLEM: EntityType<RubyGolem> =
        Registry.register(
            BuiltInRegistries.ENTITY_TYPE,
            RUBY_GOLEM_KEY,
            EntityType.Builder
                .of(
                    ::RubyGolem,
                    MobCategory.CREATURE
                )
                .sized(
                    0.8f,
                    1.4f
                )
                .build(RUBY_GOLEM_KEY)
        )

    fun initialize() {
        FabricDefaultAttributeRegistry.register(
            RUBY_GOLEM,
            RubyGolem.createAttributes()
        )
    }
}