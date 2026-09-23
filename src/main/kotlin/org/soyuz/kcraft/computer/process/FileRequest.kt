package org.soyuz.kcraft.computer.process

import java.util.concurrent.CompletableFuture

sealed interface FileRequest : ComputerRequest {
    val path: String

    data class ReadFile(
        override val path: String,
        val result: CompletableFuture<String>
    ) : FileRequest

    data class WriteFile(
        override val path: String,
        val content: String,
        val result: CompletableFuture<Unit>
    ) : FileRequest

    data class AppendFile(
        override val path: String,
        val content: String,
        val result: CompletableFuture<Unit>
    ) : FileRequest

    data class FileExists(
        override val path: String,
        val result: CompletableFuture<Boolean>
    ) : FileRequest

    data class IsDirectory(
        override val path: String,
        val result: CompletableFuture<Boolean>
    ) : FileRequest

    data class ListDirectory(
        override val path: String,
        val result: CompletableFuture<List<String>>
    ) : FileRequest

    data class CreateFile(
        override val path: String,
        val result: CompletableFuture<Unit>
    ) : FileRequest

    data class CreateDirectory(
        override val path: String,
        val result: CompletableFuture<Unit>
    ) : FileRequest

    data class DeleteFile(
        override val path: String,
        val result: CompletableFuture<Unit>
    ) : FileRequest

    data class DeleteDirectory(
        override val path: String,
        val result: CompletableFuture<Unit>
    ) : FileRequest
}