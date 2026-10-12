package com.nordtafl.features.auth.domain.port.out

interface CloudinaryPort {

    fun uploadAvatar(
        userId: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray
    ): String

    fun deleteAvatar(userId: String)
}