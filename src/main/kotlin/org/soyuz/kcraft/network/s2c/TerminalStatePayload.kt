package org.soyuz.kcraft.network.s2c

import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.Identifier
import org.soyuz.kcraft.KCraft
import org.soyuz.kcraft.computer.Terminal

data class TerminalStatePayload(
    val lines: List<String>,
    val cursorRow: Int,
    val cursorColumn: Int
) : CustomPacketPayload {

    companion object {
        val TYPE =
            CustomPacketPayload.Type<TerminalStatePayload>(
                Identifier.fromNamespaceAndPath(
                    KCraft.MOD_ID,
                    "terminal/state"
                )
            )

        private val LINES_CODEC =
            ByteBufCodecs
                .stringUtf8(Terminal.MAX_LINE_LENGTH)
                .apply(
                    ByteBufCodecs.list(
                        Terminal.VISIBLE_LINES
                    )
                )

        val STREAM_CODEC:
                StreamCodec<RegistryFriendlyByteBuf, TerminalStatePayload> =
            StreamCodec.composite(
                LINES_CODEC,
                TerminalStatePayload::lines,

                ByteBufCodecs.VAR_INT,
                TerminalStatePayload::cursorRow,

                ByteBufCodecs.VAR_INT,
                TerminalStatePayload::cursorColumn,

                ::TerminalStatePayload
            )
    }

    override fun type():
            CustomPacketPayload.Type<out CustomPacketPayload> =
        TYPE
}