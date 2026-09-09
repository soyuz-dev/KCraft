package org.soyuz.kcraft.computer.ksh

sealed interface KShToken {
    val value: String

    data class Word(
        override val value: String
    ) : KShToken

    data class StringLiteral(
        override val value: String
    ) : KShToken
}