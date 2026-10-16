package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "webauthn_credentials", schema = "auth")
class WebAuthnCredentialJpaEntity(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(name = "user_id", nullable = false)
    var userId: UUID = UUID.randomUUID(),

    @Column(name = "credential_id", nullable = false, unique = true)
    var credentialId: String = "",

    @Column(name = "public_key", nullable = false, columnDefinition = "TEXT")
    var publicKey: String = "",

    @Column(name = "sign_count", nullable = false)
    var signCount: Long = 0,

    @Column(name = "aaguid")
    var aaguid: String? = null,

    @Column(name = "transports", columnDefinition = "TEXT")
    var transports: String? = null,

    @Column(name = "attestation_type", length = 30)
    var attestationType: String? = null,

    @Column(name = "nombre_dispositivo", length = 100)
    var nombreDispositivo: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @Column(name = "last_used_at")
    var lastUsedAt: Instant? = null
)