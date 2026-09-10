package org.soyuz.kcraft.computer

class ComputerRuntime(
    val terminal: Terminal,
    val fileSystem: FileSystem
) {
    var workingDirectory: String = "/"
        private set


    fun changeDirectory(path: String): Boolean {
        val normalized = fileSystem.normalizePath(
            workingDirectory,
            path
        )

        if (!fileSystem.exists(normalized)) {
            return false
        }

        if (!fileSystem.isDirectory(normalized)) {
            return false
        }

        workingDirectory = normalized
        return true
    }

    fun openNano(path: String) {
        // later: switch runtime/screen mode
    }
}