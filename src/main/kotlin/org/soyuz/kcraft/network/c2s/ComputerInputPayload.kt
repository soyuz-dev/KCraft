package org.soyuz.kcraft.network.c2s

import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import org.soyuz.kcraft.computer.ComputerInput
import org.soyuz.kcraft.util.packet

data class ComputerInputPayload(
    val type: Type,
    val character: Int = 0
) : CustomPacketPayload {

    enum class Type {
        CHARACTER,
        BACKSPACE,
        ENTER,
        UP,
        DOWN,
        LEFT,
        RIGHT,
        SAVE,
        EXIT
    }

    companion object {
        val TYPE = CustomPacketPayload.Type<ComputerInputPayload>(
            packet("terminal/input")
        )

        val STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT,
            { payload: ComputerInputPayload -> payload.type.ordinal },

            ByteBufCodecs.VAR_INT,
            ComputerInputPayload::character,

            { typeOrdinal, character ->
                ComputerInputPayload(
                    Type.entries[typeOrdinal],
                    character
                )
            }
        )
    }

    override fun type() = TYPE
}


fun ComputerInputPayload.toComputerInput(): ComputerInput =
    when (type) {
        ComputerInputPayload.Type.CHARACTER ->
            ComputerInput.Character(character)

        ComputerInputPayload.Type.ENTER ->
            ComputerInput.Enter

        ComputerInputPayload.Type.BACKSPACE ->
            ComputerInput.Backspace

        ComputerInputPayload.Type.UP ->
            ComputerInput.Up

        ComputerInputPayload.Type.DOWN ->
            ComputerInput.Down

        ComputerInputPayload.Type.LEFT ->
            ComputerInput.Left

        ComputerInputPayload.Type.RIGHT ->
            ComputerInput.Right

        ComputerInputPayload.Type.SAVE ->
            ComputerInput.Save

        ComputerInputPayload.Type.EXIT ->
            ComputerInput.Exit
    }