package com.nordtafl.features.auth.domain.port.out

import java.util.UUID

data class StoredRefreshToken(
    val id: UUID,
    val userId: UUID,
    val tokenHash: String,
    val revoked: Boolean,
    val expiresAt: java.time.Instant
)

interface RefreshTokenRepositoryPort {

    fun save(token: StoredRefreshToken): StoredRefreshToken

    fun findByHash(tokenHash: String): StoredRefreshToken?

    fun revoke(tokenHash: String, reason: String)

    fun revokeAllByUserId(userId: UUID, reason: String)
}