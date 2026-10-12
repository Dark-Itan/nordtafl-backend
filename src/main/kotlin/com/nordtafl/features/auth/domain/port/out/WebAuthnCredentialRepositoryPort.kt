package com.nordtafl.features.auth.domain.port.out

import java.time.Instant
import java.util.UUID

data class StoredWebAuthnCredential(
    val id: UUID,
    val userId: UUID,
    val credentialId: String,
    val publicKey: String,
    val signCount: Long,
    val transports: List<String>,
    val createdAt: Instant,
    val lastUsedAt: Instant?
)

interface WebAuthnCredentialRepositoryPort {

    fun save(credential: StoredWebAuthnCredential): StoredWebAuthnCredential

    fun findByCredentialId(credentialId: String): StoredWebAuthnCredential?

    fun findAllByUserId(userId: UUID): List<StoredWebAuthnCredential>

    fun existsByUserId(userId: UUID): Boolean

    fun updateSignCount(credentialId: String, newSignCount: Long)
}