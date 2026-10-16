package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "otp_codes", schema = "auth")
class OtpJpaEntity(

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(name = "user_id")
    var userId: UUID? = null,

    @Column(name = "correo", nullable = false, length = 254)
    var correo: String = "",

    @Column(name = "codigo_hash", nullable = false, length = 255)
    var codigoHash: String = "",

    @Column(name = "proposito", nullable = false, length = 30)
    var proposito: String = "",

    @Column(name = "intentos", nullable = false)
    var intentos: Int = 0,

    @Column(name = "max_intentos", nullable = false)
    var maxIntentos: Int = 5,

    @Column(name = "usado", nullable = false)
    var usado: Boolean = false,

    @Column(name = "expira_en", nullable = false)
    var expiraEn: Instant = Instant.now(),

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null
)