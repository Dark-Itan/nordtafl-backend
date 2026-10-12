package com.nordtafl.features.auth.domain.port.`in`

import java.util.UUID

data class UploadAvatarCommand(
    val userId: UUID,
    val fileName: String,
    val contentType: String,
    val fileBytes: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is UploadAvatarCommand) return false
        return userId == other.userId &&
                fileName == other.fileName &&
                contentType == other.contentType &&
                fileBytes.contentEquals(other.fileBytes)
    }

    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = 31 * result + fileName.hashCode()
        result = 31 * result + contentType.hashCode()
        result = 31 * result + fileBytes.contentHashCode()
        return result
    }
}

data class UploadAvatarResult(
    val avatarUrl: String,
    val mensaje: String
)

interface UploadAvatarUseCase {

    fun uploadAvatar(command: UploadAvatarCommand): UploadAvatarResult
}