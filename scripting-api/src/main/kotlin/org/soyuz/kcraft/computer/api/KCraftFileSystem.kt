package org.soyuz.kcraft.computer.api

interface KCraftFileSystem {

    fun read(path: String): String

    fun write(
        path: String,
        content: String
    )

    fun append(
        path: String,
        content: String
    )

    fun exists(path: String): Boolean

    fun isDirectory(path: String): Boolean

    fun list(path: String): List<String>

    fun createFile(path: String)

    fun createDirectory(path: String)

    fun deleteFile(path: String)

    fun deleteDirectory(path: String)
}