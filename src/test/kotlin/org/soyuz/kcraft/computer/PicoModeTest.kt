package org.soyuz.kcraft.computer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path

class PicoModeTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var fileSystem: FileSystem
    private lateinit var terminal: Terminal
    private lateinit var runtime: ComputerRuntime

    @BeforeEach
    fun setUp() {
        fileSystem = FileSystem(
            tempDir.resolve("computer")
        )

        terminal = Terminal()

        runtime = ComputerRuntime(
            terminal = terminal,
            fileSystem = fileSystem
        )
    }

    private fun pico(
        contents: String = ""
    ): PicoMode =
        PicoMode(
            path = "/home/test.ksh",
            contents = contents
        )

    private fun type(
        pico: PicoMode,
        text: String
    ) {
        for (codepoint in text.codePoints().toArray()) {
            pico.handleInput(
                ComputerInput.Character(codepoint),
                runtime
            )
        }
    }

    @Test
    fun `empty document contains one editable line`() {
        val pico = pico()

        val state = pico.displayState()

        assertEquals("", state.lines.first())
        assertEquals(0, state.cursorRow)
        assertEquals(0, state.cursorColumn)
    }

    @Test
    fun `character input inserts text`() {
        val pico = pico()

        type(pico, "hello")

        val state = pico.displayState()

        assertEquals("hello", state.lines[0])
        assertEquals(0, state.cursorRow)
        assertEquals(5, state.cursorColumn)
    }

    @Test
    fun `character inserts at cursor`() {
        val pico = pico()

        type(pico, "helo")

        pico.handleInput(
            ComputerInput.Left,
            runtime
        )

        type(pico, "l")

        val state = pico.displayState()

        assertEquals("hello", state.lines[0])
        assertEquals(4, state.cursorColumn)
    }

    @Test
    fun `enter splits line at cursor`() {
        val pico = pico()

        type(pico, "helloworld")

        repeat(5) {
            pico.handleInput(
                ComputerInput.Left,
                runtime
            )
        }

        pico.handleInput(
            ComputerInput.Enter,
            runtime
        )

        val state = pico.displayState()

        assertEquals("hello", state.lines[0])
        assertEquals("world", state.lines[1])

        assertEquals(1, state.cursorRow)
        assertEquals(0, state.cursorColumn)
    }

    @Test
    fun `backspace removes previous character`() {
        val pico = pico()

        type(pico, "hello")

        pico.handleInput(
            ComputerInput.Backspace,
            runtime
        )

        val state = pico.displayState()

        assertEquals("hell", state.lines[0])
        assertEquals(4, state.cursorColumn)
    }

    @Test
    fun `backspace at start of line joins lines`() {
        val pico = pico(
            """
            hello
            world
            """.trimIndent()
        )

        // Start at row 0, column 0.
        // Move down to row 1 while remaining at column 0.
        pico.handleInput(
            ComputerInput.Down,
            runtime
        )

        pico.handleInput(
            ComputerInput.Backspace,
            runtime
        )

        val state = pico.displayState()

        assertEquals(
            "helloworld",
            state.lines[0]
        )

        assertEquals(0, state.cursorRow)
        assertEquals(5, state.cursorColumn)
    }

    @Test
    fun `backspace at start of document does nothing`() {
        val pico = pico("hello")

        pico.handleInput(
            ComputerInput.Backspace,
            runtime
        )

        val state = pico.displayState()

        assertEquals("hello", state.lines[0])
        assertEquals(0, state.cursorRow)
        assertEquals(0, state.cursorColumn)
    }

    @Test
    fun `left crosses to previous line`() {
        val pico = pico(
            """
            abc
            def
            """.trimIndent()
        )

        pico.handleInput(
            ComputerInput.Down,
            runtime
        )

        // row 1, column 0
        pico.handleInput(
            ComputerInput.Left,
            runtime
        )

        val state = pico.displayState()

        assertEquals(0, state.cursorRow)
        assertEquals(3, state.cursorColumn)
    }

    @Test
    fun `right crosses to next line`() {
        val pico = pico(
            """
            abc
            def
            """.trimIndent()
        )

        repeat(3) {
            pico.handleInput(
                ComputerInput.Right,
                runtime
            )
        }

        // End of first line.
        pico.handleInput(
            ComputerInput.Right,
            runtime
        )

        val state = pico.displayState()

        assertEquals(1, state.cursorRow)
        assertEquals(0, state.cursorColumn)
    }

    @Test
    fun `vertical movement clamps cursor to shorter line`() {
        val pico = pico(
            """
            abcdef
            xy
            """.trimIndent()
        )

        repeat(5) {
            pico.handleInput(
                ComputerInput.Right,
                runtime
            )
        }

        assertEquals(
            5,
            pico.displayState().cursorColumn
        )

        pico.handleInput(
            ComputerInput.Down,
            runtime
        )

        val state = pico.displayState()

        assertEquals(1, state.cursorRow)
        assertEquals(2, state.cursorColumn)
    }

    @Test
    fun `cursor cannot move above first line`() {
        val pico = pico("hello")

        pico.handleInput(
            ComputerInput.Up,
            runtime
        )

        val state = pico.displayState()

        assertEquals(0, state.cursorRow)
        assertEquals(0, state.cursorColumn)
    }

    @Test
    fun `cursor cannot move below last line`() {
        val pico = pico("hello")

        pico.handleInput(
            ComputerInput.Down,
            runtime
        )

        val state = pico.displayState()

        assertEquals(0, state.cursorRow)
        assertEquals(0, state.cursorColumn)
    }

    @Test
    fun `line length is limited`() {
        val pico = pico()

        type(
            pico,
            "x".repeat(
                PicoMode.MAX_LINE_LENGTH + 50
            )
        )

        val state = pico.displayState()

        assertEquals(
            PicoMode.MAX_LINE_LENGTH,
            state.lines[0].length
        )

        assertEquals(
            PicoMode.MAX_LINE_LENGTH,
            state.cursorColumn
        )
    }

    @Test
    fun `enter preserves text on both sides of cursor`() {
        val pico = pico("abcdef")

        repeat(3) {
            pico.handleInput(
                ComputerInput.Right,
                runtime
            )
        }

        pico.handleInput(
            ComputerInput.Enter,
            runtime
        )

        val state = pico.displayState()

        assertEquals("abc", state.lines[0])
        assertEquals("def", state.lines[1])
    }

    @Test
    fun `save writes document to filesystem`() {
        val pico = pico()

        type(pico, "echo hello")

        pico.handleInput(
            ComputerInput.Enter,
            runtime
        )

        type(pico, "echo world")

        pico.handleInput(
            ComputerInput.Save,
            runtime
        )

        assertEquals(
            """
            echo hello
            echo world
            """.trimIndent(),
            fileSystem.readFile(
                "/home/test.ksh"
            )
        )
    }

    @Test
    fun `save overwrites existing file`() {
        fileSystem.writeFile(
            "/home/test.ksh",
            "old garbage"
        )

        val pico = pico("new contents")

        pico.handleInput(
            ComputerInput.Save,
            runtime
        )

        assertEquals(
            "new contents",
            fileSystem.readFile(
                "/home/test.ksh"
            )
        )
    }

    @Test
    fun `exit returns runtime to terminal`() {
        runtime.openPico(
            "/home/test.ksh"
        )

        assertTrue(
            runtime.activeMode is PicoMode
        )

        runtime.handleInput(
            ComputerInput.Exit
        )

        assertSame(
            terminal,
            runtime.activeMode
        )
    }

    @Test
    fun `viewport follows cursor downward`() {
        val contents =
            (0 until PicoMode.VISIBLE_LINES + 5)
                .joinToString("\n") {
                    "Line $it"
                }

        val pico = pico(contents)

        repeat(PicoMode.VISIBLE_LINES + 3) {
            pico.handleInput(
                ComputerInput.Down,
                runtime
            )
        }

        val state = pico.displayState()

        assertNotEquals(
            "Line 0",
            state.lines.first()
        )

        assertTrue(
            state.cursorRow in
                    0 until PicoMode.VISIBLE_LINES
        )
    }

    @Test
    fun `viewport follows cursor upward`() {
        val contents =
            (0 until PicoMode.VISIBLE_LINES + 5)
                .joinToString("\n") {
                    "Line $it"
                }

        val pico = pico(contents)

        repeat(PicoMode.VISIBLE_LINES + 4) {
            pico.handleInput(
                ComputerInput.Down,
                runtime
            )
        }

        repeat(PicoMode.VISIBLE_LINES + 4) {
            pico.handleInput(
                ComputerInput.Up,
                runtime
            )
        }

        val state = pico.displayState()

        assertEquals(
            "Line 0",
            state.lines.first()
        )

        assertEquals(0, state.cursorRow)
    }

    @Test
    fun `display always contains visible line count`() {
        val pico = pico("hello")

        assertEquals(
            PicoMode.VISIBLE_LINES,
            pico.displayState().lines.size
        )
    }
}