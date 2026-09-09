package org.soyuz.kcraft.computer.ksh



import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class KshLexerTest {
    @Test
    fun `lexes plain words`() {
        assertEquals(
            listOf(
                KShToken.Word("echo"),
                KShToken.Word("hello")
            ),
            KShLexer.lex("echo hello")
        )
    }

    @Test
    fun `lexes quoted strings`() {
        assertEquals(
            listOf(
                KShToken.Word("echo"),
                KShToken.StringLiteral("hello world")
            ),
            KShLexer.lex("""echo "hello world"""")
        )
    }

    @Test
    fun `handles escaped quotes`() {
        assertEquals(
            listOf(
                KShToken.Word("echo"),
                KShToken.StringLiteral("""hello "world"""")
            ),
            KShLexer.lex("""echo "hello \"world\""""")
        )
    }

    @Test
    fun `ignores excess whitespace`() {
        assertEquals(
            listOf(
                KShToken.Word("run"),
                KShToken.Word("/home/test.kts")
            ),
            KShLexer.lex("   run    /home/test.kts   ")
        )
    }

    @Test
    fun `rejects unterminated strings`() {
        assertThrows<IllegalStateException> {
            KShLexer.lex("""echo "hello""")
        }
    }
}