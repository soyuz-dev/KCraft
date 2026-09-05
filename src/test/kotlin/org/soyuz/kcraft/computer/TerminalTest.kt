package org.soyuz.kcraft.computer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.soyuz.kcraft.computer.Terminal

class TerminalTest {

    private lateinit var terminal: Terminal

    @BeforeEach
    fun setUp() {
        terminal = Terminal()
    }

    @Test
    fun `test initial terminal state is completely empty`() {
        assertEquals(0, terminal.lineCount)
        assertEquals(Terminal.CursorPosition(0, 0), terminal.cursorPosition)
        assertTrue(terminal.lines.isEmpty())

        // Ensure visible lines are all filled with empty strings
        val visible = terminal.visibleLines
        assertEquals(Terminal.VISIBLE_LINES, visible.size)
        assertTrue(visible.all { it == "" })
    }

    @Test
    fun `test appendChar adds character and advances cursor column`() {
        terminal.appendLine("") // Create an active empty line to write to
        terminal.appendChar('A')
        terminal.appendChar('B')

        assertEquals(1, terminal.lineCount)
        assertEquals("AB", terminal.lines[0])
        assertEquals(Terminal.CursorPosition(0, 2), terminal.cursorPosition)
    }

    @Test
    fun `test appendChar wraps to a new line when max line length is exceeded`() {
        terminal.appendLine("") // Set up first line

        // Fill the first line completely to its limit
        repeat(Terminal.MAX_LINE_LENGTH) { terminal.appendChar('X') }
        assertEquals(1, terminal.lineCount)
        assertEquals(Terminal.MAX_LINE_LENGTH, terminal.cursorPosition.column)

        // This character should break the limit and force a line wrap
        terminal.appendChar('Y')

        // Assertions
        assertEquals(2, terminal.lineCount) // A second line should have been created
        assertEquals("Y", terminal.lines[1]) // The new character lives on row index 1
        assertEquals(Terminal.CursorPosition(1, 1), terminal.cursorPosition) // Cursor moved to row 1, col 1
    }

    @Test
    fun `test popChar wraps back up to the previous line when deleting on an empty line`() {
        terminal.appendLine("") // First line

        // Fill the first line and let it auto-wrap to the second line
        repeat(Terminal.MAX_LINE_LENGTH) { terminal.appendChar('X') }
        terminal.appendChar('Y')

        // Ensure we are currently on the second line with our wrapped character
        assertEquals(2, terminal.lineCount)
        assertEquals(Terminal.CursorPosition(1, 1), terminal.cursorPosition)

        // Pop the 'Y' character
        val firstPop = terminal.popChar()
        assertEquals('Y', firstPop)
        assertEquals(Terminal.CursorPosition(1, 0), terminal.cursorPosition) // Cursor is at col 0, row 1

        // Pop again at column 0 to force backward wrap into the first line
        val secondPop = terminal.popChar()
        assertEquals('X', secondPop) // Should return the last character of the first line
        assertEquals(1, terminal.lineCount) // The empty second line should be removed
        assertEquals(Terminal.CursorPosition(0, Terminal.MAX_LINE_LENGTH - 1), terminal.cursorPosition)
    }


    @Test
    fun `test popChar removes last character and updates cursor position`() {
        terminal.appendLine("Kotlin")

        val popped = terminal.popChar()

        assertEquals('n', popped)
        assertEquals("Kotli", terminal.lines[0])
        assertEquals(Terminal.CursorPosition(0, 5), terminal.cursorPosition)
    }

    @Test
    fun `test popChar returns null when the current row is completely empty`() {
        terminal.appendLine("")
        assertNull(terminal.popChar())
    }

    @Test
    fun `test appendLine adds standard line and handles auto-chunking`() {
        terminal.appendLine("Short text")
        assertEquals(1, terminal.lineCount)
        assertEquals("Short text", terminal.lines[0])

        // Append a line that exceeds the max length to trigger chunking logic
        val longText = "A".repeat(Terminal.MAX_LINE_LENGTH + 10)
        terminal.appendLine(longText)

        // The single long line should break down into 2 logical lines
        assertEquals(3, terminal.lineCount)
        assertEquals("A".repeat(Terminal.MAX_LINE_LENGTH), terminal.lines[1])
        assertEquals("A".repeat(10), terminal.lines[2])
        assertEquals(Terminal.CursorPosition(2, 10), terminal.cursorPosition)
    }

    @Test
    fun `test popLine removes the last logical line completely`() {
        terminal.appendLine("Line 1")
        terminal.appendLine("Line 2")

        val popped = terminal.popLine()

        assertEquals("Line 2", popped)
        assertEquals(1, terminal.lineCount)
        assertEquals("Line 1", terminal.lines[0])
        assertEquals(Terminal.CursorPosition(0, 6), terminal.cursorPosition)
    }

    @Test
    fun `test circular buffer behavior drops oldest records gracefully`() {
        // Exceed the absolute total capacity of the circular layout
        val bufferCapacityOverfill = Terminal.MAX_LINES + 5
        for (i in 1..bufferCapacityOverfill) {
            terminal.appendLine("Line $i")
        }

        assertEquals(Terminal.MAX_LINES, terminal.lineCount)
        // Check that the oldest lines (Line 1 to Line 5) were overwritten
        assertEquals("Line 6", terminal.lines[0])
        assertEquals("Line $bufferCapacityOverfill", terminal.lines.last())
    }

    @Test
    fun `test clear resets all positions, counts, and contents back to zero`() {
        terminal.appendLine("Data")
        terminal.appendChar('A')
        terminal.clear()

        assertEquals(0, terminal.lineCount)
        assertEquals(Terminal.CursorPosition(0, 0), terminal.cursorPosition)
        assertTrue(terminal.lines.isEmpty())
    }

    @Test
    fun `test scroll mechanics keep operations bounded cleanly`() {
        // Add more rows than what is visibly viewable at once
        repeat(12) { terminal.appendLine("Row $it") }

        // Pushing text forces auto-scroll directly to bottom
        var visible = terminal.visibleLines
        assertEquals("Row 4", visible[0])
        assertEquals("Row 11", visible[7])

        // Check bounds enforcement by testing scrolling methods
        terminal.scrollDown(2) // Going backwards down the buffer page view index
        visible = terminal.visibleLines
        assertEquals("Row 2", visible[0])

        // Verify invalid inputs are handled securely
        assertThrows<IllegalArgumentException> {
            terminal.scrollUp(-1)
        }
    }
}
