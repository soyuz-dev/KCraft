package org.soyuz.kcraft.computer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class TerminalTest {

    private lateinit var terminal: Terminal

    @BeforeEach
    fun setUp() {
        terminal = Terminal()
    }

    @Test
    fun `initial state has no history and empty input`() {
        assertEquals(0, terminal.lineCount)
        assertTrue(terminal.lines.isEmpty())
        assertEquals("", terminal.input)
        assertEquals(0, terminal.inputCursor)

        val visible = terminal.visibleLines

        assertEquals(Terminal.VISIBLE_LINES, visible.size)
        assertEquals(Terminal.PROMPT, visible.first())
        assertTrue(visible.drop(1).all { it == "" })
    }

    @Test
    fun `appendChar edits input without touching history`() {
        terminal.appendChar('h')
        terminal.appendChar('i')

        assertEquals("hi", terminal.input)
        assertEquals(2, terminal.inputCursor)
        assertTrue(terminal.lines.isEmpty())
    }

    @Test
    fun `backspace only edits current input`() {
        terminal.appendLine("historical output")

        terminal.appendChar('a')
        terminal.appendChar('b')

        assertEquals('b', terminal.popChar())

        assertEquals("a", terminal.input)
        assertEquals(
            listOf("historical output"),
            terminal.lines
        )
    }

    @Test
    fun `backspace cannot delete terminal history`() {
        terminal.appendLine("do not eat me")

        assertNull(terminal.popChar())

        assertEquals(
            listOf("do not eat me"),
            terminal.lines
        )
    }

    @Test
    fun `commitInput adds prompt and command to history`() {
        "echo hello".forEach(terminal::appendChar)

        val command = terminal.commitInput()

        assertEquals("echo hello", command)
        assertEquals("", terminal.input)
        assertEquals(0, terminal.inputCursor)

        assertEquals(
            "> echo hello",
            terminal.lines.last()
        )
    }

    @Test
    fun `commitInput with empty input still commits prompt`() {
        val command = terminal.commitInput()

        assertEquals("", command)
        assertEquals(
            Terminal.PROMPT,
            terminal.lines.last()
        )
    }

    @Test
    fun `clear removes history and input`() {
        terminal.appendLine("hello")
        "world".forEach(terminal::appendChar)

        terminal.clear()

        assertTrue(terminal.lines.isEmpty())
        assertEquals("", terminal.input)
        assertEquals(0, terminal.inputCursor)
        assertEquals(0, terminal.lineCount)
    }

    @Test
    fun `appendLine chunks long output`() {
        val text =
            "A".repeat(Terminal.MAX_LINE_LENGTH + 10)

        terminal.appendLine(text)

        assertEquals(2, terminal.lineCount)
        assertEquals(
            "A".repeat(Terminal.MAX_LINE_LENGTH),
            terminal.lines[0]
        )
        assertEquals(
            "A".repeat(10),
            terminal.lines[1]
        )
    }

    @Test
    fun `input respects terminal width`() {
        repeat(Terminal.MAX_LINE_LENGTH * 2) {
            terminal.appendChar('X')
        }

        assertEquals(
            Terminal.MAX_LINE_LENGTH - Terminal.PROMPT.length,
            terminal.input.length
        )
    }

    @Test
    fun `cursor can move left and insert`() {
        "helo".forEach(terminal::appendChar)

        terminal.moveCursorLeft()

        terminal.appendChar('l')

        assertEquals("hello", terminal.input)
        assertEquals(4, terminal.inputCursor)
    }

    @Test
    fun `cursor cannot move before start`() {
        terminal.moveCursorLeft(100)

        assertEquals(0, terminal.inputCursor)
    }

    @Test
    fun `cursor cannot move past end`() {
        "abc".forEach(terminal::appendChar)

        terminal.moveCursorRight(100)

        assertEquals(3, terminal.inputCursor)
    }

    @Test
    fun `negative cursor movement is rejected`() {
        assertThrows<IllegalArgumentException> {
            terminal.moveCursorLeft(-1)
        }

        assertThrows<IllegalArgumentException> {
            terminal.moveCursorRight(-1)
        }
    }

    @Test
    fun `circular buffer drops oldest history lines`() {
        repeat(Terminal.MAX_LINES + 5) { index ->
            terminal.appendLine("Line ${index + 1}")
        }

        assertEquals(
            Terminal.MAX_LINES,
            terminal.lineCount
        )

        assertEquals(
            "Line 6",
            terminal.lines.first()
        )

        assertEquals(
            "Line ${Terminal.MAX_LINES + 5}",
            terminal.lines.last()
        )
    }

    @Test
    fun `visible lines include prompt line`() {
        repeat(Terminal.VISIBLE_LINES) { index ->
            terminal.appendLine("Row $index")
        }

        val visible = terminal.visibleLines

        // 8 history lines + prompt means the oldest one scrolls off.
        assertEquals("Row 1", visible.first())
        assertEquals(Terminal.PROMPT, visible.last())
    }

    @Test
    fun `scrolling moves through history`() {
        repeat(12) { index ->
            terminal.appendLine("Row $index")
        }

        var visible = terminal.visibleLines

        assertEquals("Row 5", visible.first())
        assertEquals(Terminal.PROMPT, visible.last())

        terminal.scrollUp(2)

        visible = terminal.visibleLines

        assertEquals("Row 3", visible.first())

        terminal.scrollDown(2)

        visible = terminal.visibleLines

        assertEquals("Row 5", visible.first())
    }

    @Test
    fun `negative scroll amount is rejected`() {
        assertThrows<IllegalArgumentException> {
            terminal.scrollUp(-1)
        }

        assertThrows<IllegalArgumentException> {
            terminal.scrollDown(-1)
        }
    }

    @Test
    fun `visible cursor points to prompt input position`() {
        "abc".forEach(terminal::appendChar)

        val cursor = terminal.visibleCursorPosition

        assertNotNull(cursor)

        assertEquals(
            Terminal.PROMPT.length + 3,
            cursor!!.column
        )
    }

    @Test
    fun `setInput replaces input and places cursor at end`() {
        terminal.setInput("hello")

        assertEquals("hello", terminal.input)
        assertEquals(5, terminal.inputCursor)
    }

    @Test
    fun `clearInput leaves history untouched`() {
        terminal.appendLine("history")

        "temporary".forEach(terminal::appendChar)

        terminal.clearInput()

        assertEquals("", terminal.input)
        assertEquals(0, terminal.inputCursor)
        assertEquals(
            listOf("history"),
            terminal.lines
        )
    }
}