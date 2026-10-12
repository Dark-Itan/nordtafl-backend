package com.nordtafl.features.auth.domain.port.out

import java.time.Instant
import java.util.UUID

data class StoredPasswordResetToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val usado: Boolean,
    val expiresAt: Instant
)

interface PasswordResetTokenRepositoryPort {

    fun save(
        userId: UUID,
        tokenHash: String,
        expiresAt: Instant
    ): StoredPasswordResetToken

    fun findByHash(tokenHash: String): StoredPasswordResetToken?

    fun markAsUsed(id: UUID)

    fun invalidatePrevious(userId: UUID)
}