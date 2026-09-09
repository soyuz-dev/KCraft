package org.soyuz.kcraft.computer

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.*

class FileSystemTest {

    @TempDir
    lateinit var tempDir: Path

    @Test
    fun `empty root is populated from bundled rootfs`() {
        val root = tempDir.resolve("computer")

        val fs = FileSystem(root)

        assertTrue(fs.exists("/bin/help.ksh"))
        assertTrue(fs.exists("/etc/shell.kshrc"))
    }

    @Test
    fun `non-empty root is not populated`() {
        val root = tempDir.resolve("computer")
        root.createDirectories()

        root.resolve("existing.txt")
            .writeText("do not touch")

        val fs = FileSystem(root)

        assertTrue(fs.exists("/existing.txt"))
        assertEquals(
            "do not touch",
            fs.readFile("/existing.txt")
        )

        // Because the filesystem wasn't empty, factory rootfs
        // should not have been installed.
        assertFalse(fs.exists("/bin/help.ksh"))
    }

    @Test
    fun `writeFile creates parent directories`() {
        val fs = FileSystem(tempDir.resolve("computer"))

        fs.writeFile(
            "/home/user/hello.txt",
            "Hello KCraft!"
        )

        assertTrue(fs.exists("/home/user/hello.txt"))
        assertEquals(
            "Hello KCraft!",
            fs.readFile("/home/user/hello.txt")
        )
    }

    @Test
    fun `writeFile overwrites existing file`() {
        val fs = FileSystem(tempDir.resolve("computer"))

        fs.writeFile("/test.txt", "old")
        fs.writeFile("/test.txt", "new")

        assertEquals(
            "new",
            fs.readFile("/test.txt")
        )
    }

    @Test
    fun `list returns directory contents`() {
        val fs = FileSystem(tempDir.resolve("computer"))

        fs.writeFile("/home/a.txt", "A")
        fs.writeFile("/home/b.txt", "B")

        val contents = fs.list("/home")

        assertEquals(
            setOf("a.txt", "b.txt"),
            contents.toSet()
        )
    }

    @Test
    fun `path cannot escape filesystem root`() {
        val fs = FileSystem(tempDir.resolve("computer"))

        assertThrows<IllegalArgumentException> {
            fs.writeFile(
                "/../../definitely-not-kcraft.txt",
                "GET OUT OF MY FILESYSTEM"
            )
        }
    }
}