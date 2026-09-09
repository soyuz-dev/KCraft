package org.soyuz.kcraft.computer.ksh

object KShLexer {

    fun lex(source: String): List<KShToken> {
        val tokens = mutableListOf<KShToken>()

        var index = 0

        while (index < source.length) {
            val char = source[index]

            when {
                char.isWhitespace() -> {
                    index++
                }

                char == '"' -> {
                    val result = readStringLiteral(source, index)
                    tokens += KShToken.StringLiteral(result.value)
                    index = result.nextIndex
                }

                else -> {
                    val result = readWord(source, index)
                    tokens += KShToken.Word(result.value)
                    index = result.nextIndex
                }
            }
        }

        return tokens
    }

    private fun readWord(
        source: String,
        start: Int
    ): LexResult {
        var index = start

        while (
            index < source.length &&
            !source[index].isWhitespace() &&
            source[index] != '"'
        ) {
            index++
        }

        return LexResult(
            value = source.substring(start, index),
            nextIndex = index
        )
    }

    private fun readStringLiteral(
        source: String,
        start: Int
    ): LexResult {
        val output = StringBuilder()

        var index = start + 1

        while (index < source.length) {
            val char = source[index]

            when {
                char == '"' -> {
                    return LexResult(
                        value = output.toString(),
                        nextIndex = index + 1
                    )
                }

                char == '\\' -> {
                    require(index + 1 < source.length) {
                        "Unterminated escape sequence"
                    }

                    val escaped = source[index + 1]

                    output.append(
                        when (escaped) {
                            '"' -> '"'
                            '\\' -> '\\'
                            'n' -> '\n'
                            't' -> '\t'
                            else -> escaped
                        }
                    )

                    index += 2
                }

                else -> {
                    output.append(char)
                    index++
                }
            }
        }

        error("Unterminated string literal")
    }

    private data class LexResult(
        val value: String,
        val nextIndex: Int
    )
}