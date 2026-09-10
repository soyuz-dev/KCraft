package org.soyuz.kcraft.computer.ksh

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import org.soyuz.kcraft.computer.ComputerRuntime
import org.soyuz.kcraft.computer.FileSystem
import org.soyuz.kcraft.computer.Terminal
import java.nio.file.Path

class KShInterpreterTest {

    @TempDir
    lateinit var tempDir: Path

    private lateinit var terminal: Terminal
    private lateinit var fileSystem: FileSystem
    private lateinit var runtime: ComputerRuntime
    private lateinit var interpreter: KShInterpreter

    @BeforeEach
    fun setUp() {
        terminal = Terminal()
        fileSystem = FileSystem(tempDir.resolve("computer"))

        runtime = ComputerRuntime(
            terminal,
            fileSystem
        )

        interpreter = KShInterpreter(
            runtime
        )
    }

    @Test
    fun `echo prints its arguments`() {
        interpreter.executeLine("""echo "Hello world" from KSh""")

        assertEquals(
            "Hello world from KSh",
            terminal.lines.last()
        )
    }

    @Test
    fun `clear clears terminal`() {
        interpreter.executeLine("echo hello")
        interpreter.executeLine("echo world")

        assertTrue(terminal.lines.isNotEmpty())

        interpreter.executeLine("clear")

        assertTrue(terminal.lines.isEmpty())
    }

    @Test
    fun `clear rejects arguments`() {
        interpreter.executeLine("clear definitely")

        assertEquals(
            "clear: expected no arguments",
            terminal.lines.last()
        )
    }

    @Test
    fun `unknown command reports error`() {
        interpreter.executeLine("summon_the_kraken")

        assertEquals(
            "ksh: command not found: summon_the_kraken",
            terminal.lines.last()
        )
    }

    @Test
    fun `source executes a ksh file`() {
        fileSystem.writeFile(
            "/test.ksh",
            """
            echo "first line"
            echo "second line"
            """.trimIndent()
        )

        interpreter.executeLine("source /test.ksh")

        assertEquals(
            listOf(
                "first line",
                "second line"
            ),
            terminal.lines.takeLast(2)
        )
    }

    @Test
    fun `source ignores blank lines and comments`() {
        fileSystem.writeFile(
            "/test.ksh",
            """
            # This is a comment

            echo first

            # Another comment
            echo second
            """.trimIndent()
        )

        interpreter.executeLine("source /test.ksh")

        assertEquals(
            listOf("first", "second"),
            terminal.lines.takeLast(2)
        )
    }

    @Test
    fun `recursive source eventually drowns in the depths`() {
        fileSystem.writeFile(
            "/recursive.ksh",
            "source /recursive.ksh"
        )

        interpreter.executeLine(
            "source /recursive.ksh"
        )

        assertEquals(
            "source: drowning in the depths",
            terminal.lines.last()
        )
    }

    @Test
    fun `touch creates file in working directory`() {
        interpreter.executeLine("mkdir /home/test")
        interpreter.executeLine("cd /home/test")
        interpreter.executeLine("touch hello.ksh")

        assertTrue(
            fileSystem.exists("/home/test/hello.ksh")
        )
    }

    @Test
    fun `touch does not create missing parent directories`() {
        interpreter.executeLine(
            "touch /does/not/exist/test.ksh"
        )

        assertFalse(
            fileSystem.exists(
                "/does/not/exist/test.ksh"
            )
        )

        assertTrue(
            terminal.lines.last()
                .startsWith("touch:")
        )
    }

    @Test
    fun `mkdir creates directory`() {
        interpreter.executeLine(
            "mkdir /home/projects"
        )

        assertTrue(
            fileSystem.exists("/home/projects")
        )

        assertTrue(
            fileSystem.isDirectory("/home/projects")
        )
    }

    @Test
    fun `cd changes working directory`() {
        interpreter.executeLine(
            "mkdir /home/projects"
        )

        interpreter.executeLine(
            "cd /home/projects"
        )

        assertEquals(
            "/home/projects",
            runtime.workingDirectory
        )
    }

    @Test
    fun `cd supports parent directory`() {
        interpreter.executeLine(
            "mkdir /home/projects"
        )

        interpreter.executeLine(
            "cd /home/projects"
        )

        interpreter.executeLine("cd ..")

        assertEquals(
            "/home",
            runtime.workingDirectory
        )
    }

    @Test
    fun `cd refuses regular files`() {
        fileSystem.createFile("/home/not-a-directory")

        interpreter.executeLine(
            "cd /home/not-a-directory"
        )

        assertEquals(
            "/",
            runtime.workingDirectory
        )

        assertTrue(
            terminal.lines.last()
                .startsWith("cd: not a directory:")
        )
    }

    @Test
    fun `pwd prints working directory`() {
        interpreter.executeLine(
            "mkdir /home/projects"
        )

        interpreter.executeLine(
            "cd /home/projects"
        )

        interpreter.executeLine("pwd")

        assertEquals(
            "/home/projects",
            terminal.lines.last()
        )
    }

    @Test
    fun `relative paths resolve against working directory`() {
        interpreter.executeLine(
            "mkdir /home/projects"
        )

        interpreter.executeLine(
            "cd /home/projects"
        )

        interpreter.executeLine(
            "mkdir scripts"
        )

        interpreter.executeLine(
            "touch scripts/test.ksh"
        )

        assertTrue(
            fileSystem.exists(
                "/home/projects/scripts/test.ksh"
            )
        )
    }
}