package org.soyuz.kcraft.network.c2s

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import org.soyuz.kcraft.util.packet

data object DeployGolemPayload : CustomPacketPayload {

    val TYPE =
        CustomPacketPayload.Type<DeployGolemPayload>(
            packet("golem/deploy")
        )

    val STREAM_CODEC:
            StreamCodec<
                    RegistryFriendlyByteBuf,
                    DeployGolemPayload
                    > =
        StreamCodec.unit(
            DeployGolemPayload
        )

    override fun type() =
        TYPE
}