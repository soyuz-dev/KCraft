package org.soyuz.kcraft.network.c2s

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import org.soyuz.kcraft.util.packet

data object RequestComputerDisplayStatePayload : CustomPacketPayload {

    val TYPE =
            CustomPacketPayload.Type<RequestComputerDisplayStatePayload>(
                packet("terminal/request_state")
            )

    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, RequestComputerDisplayStatePayload> =
            StreamCodec.unit(RequestComputerDisplayStatePayload)


    override fun type() = TYPE
}