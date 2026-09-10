package org.soyuz.kcraft.network.c2s

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import org.soyuz.kcraft.util.packet

data object RequestTerminalStatePayload : CustomPacketPayload {

    val TYPE =
            CustomPacketPayload.Type<RequestTerminalStatePayload>(
                packet("terminal/request_state")
            )

    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, RequestTerminalStatePayload> =
            StreamCodec.unit(RequestTerminalStatePayload)


    override fun type() = TYPE
}