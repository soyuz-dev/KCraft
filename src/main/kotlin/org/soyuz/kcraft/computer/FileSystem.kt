package org.soyuz.kcraft.computer

import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.createDirectory
import kotlin.io.path.createFile
import kotlin.io.path.exists
import kotlin.io.path.isDirectory
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.outputStream
import kotlin.io.path.readText
import kotlin.io.path.writeText

class FileSystem(
    val root: Path
) {
    companion object {
        private const val ROOTFS_RESOURCE =
            "kcraft/computer/rootfs"

        private const val ROOTFS_INDEX =
            "$ROOTFS_RESOURCE/rootfs.index"
    }

    init {
        root.createDirectories()

        if (root.listDirectoryEntries().isEmpty()) {
            populateRootFs()
        }
    }

    fun readFile(path: String): String =
        resolve(path).readText()

    fun writeFile(
        path: String,
        content: String
    ) {
        val file = resolve(path)

        file.parent?.createDirectories()
        file.writeText(content)
    }

    fun exists(path: String): Boolean =
        resolve(path).exists()

    fun list(path: String): List<String> =
        resolve(path)
            .listDirectoryEntries()
            .map { it.fileName.toString() }

    fun isDirectory(path: String): Boolean =
        resolve(path).isDirectory()

    private fun populateRootFs() {
        createBaseDirectories()

        val classLoader = javaClass.classLoader

        val index = classLoader
            .getResourceAsStream(ROOTFS_INDEX)
            ?: error("Missing KCraft rootfs index: $ROOTFS_INDEX")

        val files = index.bufferedReader().useLines { lines ->
            lines
                .map(String::trim)
                .filter { it.isNotEmpty() }
                .filterNot { it.startsWith("#") }
                .toList()
        }

        for (relativePath in files) {
            copyRootFsFile(relativePath)
        }
    }

    private fun createBaseDirectories() {
        listOf(
            "/bin",
            "/etc",
            "/home",
            "/tmp",
            "/dev"
        ).forEach { path ->
            resolve(path).createDirectories()
        }
    }

    private fun copyRootFsFile(relativePath: String) {
        val resourcePath =
            "$ROOTFS_RESOURCE/$relativePath"

        val input = javaClass.classLoader
            .getResourceAsStream(resourcePath)
            ?: error("Missing KCraft rootfs file: $resourcePath")

        val destination = resolve(relativePath)

        destination.parent?.createDirectories()

        input.use {
            destination.outputStream().use { output ->
                it.copyTo(output)
            }
        }
    }

    private fun resolve(path: String): Path {
        val normalizedRoot =
            root.toAbsolutePath().normalize()

        val resolved = normalizedRoot
            .resolve(path.removePrefix("/"))
            .normalize()

        require(resolved.startsWith(normalizedRoot)) {
            "Path escapes filesystem root: $path"
        }

        return resolved
    }

    fun normalizePath(
        workingDirectory: String,
        path: String
    ): String {
        val combined = if (path.startsWith("/")) {
            path
        } else {
            "$workingDirectory/$path"
        }
        val parts = mutableListOf<String>()

        for (part in combined.split('/')) {
            when (part) {
                "", "." -> Unit

                ".." -> {
                    if (parts.isNotEmpty()) {
                        parts.removeLast()
                    }
                }
                else -> parts += part
            }
        }
        return "/" + parts.joinToString("/")
    }

    fun createFile(path: String) {
        val file = resolve(path)
        val parent = file.parent

        require(parent != null && parent.exists()) {
            "Parent directory does not exist: $path"
        }

        require(parent.isDirectory()) {
            "Parent is not a directory: $path"
        }

        if (!file.exists()) {
            require(!file.isDirectory()) {
                "Is a directory: $path"
            }
            file.createFile()
        }
    }

    fun createDirectory(path: String) {
        val directory = resolve(path)
        val parent = directory.parent

        require(parent != null && parent.exists()) {
            "Parent directory does not exist: $path"
        }

        require(parent.isDirectory()) {
            "Parent is not a directory: $path"
        }

        require(!directory.exists()) {
            "File or directory already exists: $path"
        }

        directory.createDirectory()
    }

}