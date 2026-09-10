package org.soyuz.kcraft.network.c2s

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import org.soyuz.kcraft.util.packet

data class TerminalInputPayload(
    val type: Type,
    val character: Int = 0
) : CustomPacketPayload {

    enum class Type {
        CHARACTER,
        BACKSPACE,
        ENTER,
        SCROLL_UP,
        SCROLL_DOWN
    }

    companion object {
        val TYPE = CustomPacketPayload.Type<TerminalInputPayload>(
            packet("terminal/input")
        )

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            { payload: TerminalInputPayload -> payload.type.ordinal },

            ByteBufCodecs.VAR_INT,
            TerminalInputPayload::character,

            { typeOrdinal, character ->
                TerminalInputPayload(
                    Type.entries[typeOrdinal],
                    character
                )
            }
        )
    }

    override fun type() = TYPE
}