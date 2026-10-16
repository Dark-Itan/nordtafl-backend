package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "password_reset_tokens", schema = "auth")
class PasswordResetTokenJpaEntity(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID(),

    @Column(name = "token_hash", nullable = false, unique = true, length = 255)
    var tokenHash: String = "",

    @Column(name = "usado", nullable = false)
    var usado: Boolean = false,

    @Column(name = "usado_en")
    var usadoEn: Instant? = null,

    @Column(name = "expira_en", nullable = false)
    var expiresAt: Instant = Instant.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
)