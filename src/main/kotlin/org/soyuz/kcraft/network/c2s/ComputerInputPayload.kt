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

    fun toComputerInput(): ComputerInput =
        when (type) {
            Type.CHARACTER ->
                ComputerInput.Character(character)

            Type.ENTER ->
                ComputerInput.Enter

            Type.BACKSPACE ->
                ComputerInput.Backspace

            Type.UP ->
                ComputerInput.Up

            Type.DOWN ->

                ComputerInput.Down

            Type.LEFT ->
                ComputerInput.Left

            Type.RIGHT ->
                ComputerInput.Right

            Type.SAVE ->
                ComputerInput.Save

            ComputerInputPayload.Type.EXIT ->
                ComputerInput.Exit
        }
}