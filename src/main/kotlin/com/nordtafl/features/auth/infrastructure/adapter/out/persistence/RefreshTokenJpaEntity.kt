package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "refresh_tokens", schema = "auth")
class RefreshTokenJpaEntity(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID(),

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    var tokenHash: String = "",

    @Column(name = "revoked", nullable = false)
    var revoked: Boolean = false,

    @Column(name = "revoked_at")
    var revokedAt: Instant? = null,

    @Column(name = "revoked_reason", length = 100)
    var revokedReason: String? = null,

    @Column(name = "expira_en", nullable = false)
    var expiresAt: Instant = Instant.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "last_used_at")
    var lastUsedAt: Instant? = null
)