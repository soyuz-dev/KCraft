package org.soyuz.kcraft.client.golem

import net.minecraft.client.model.animal.golem.IronGolemModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.state.IronGolemRenderState
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.resources.Identifier
import org.soyuz.kcraft.golem.RubyGolem

class RubyGolemRenderer(
    context: EntityRendererProvider.Context
) : MobRenderer<
        RubyGolem,
        IronGolemRenderState,
        IronGolemModel
        >(
    context,
    IronGolemModel(
        context.bakeLayer(
            ModelLayers.IRON_GOLEM
        )
    ),
    0.7f
) {

    companion object {
        private val TEXTURE =
            Identifier.fromNamespaceAndPath(
                "kcraft",
                "textures/entity/ruby_golem/ruby_golem.png"
            )
    }

    override fun getTextureLocation(
        state: IronGolemRenderState
    ): Identifier =
        TEXTURE

    override fun createRenderState():
            IronGolemRenderState =
        IronGolemRenderState()

}