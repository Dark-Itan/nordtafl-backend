package com.nordtafl.features.auth.domain.port.out

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.`in`.OtpPurpose
import java.time.Instant
import java.util.UUID

data class StoredOtp(
    val id: UUID,
    val userId: UUID?,
    val correo: Email,
    val codigoHash: String,
    val proposito: OtpPurpose,
    val intentos: Int,
    val maxIntentos: Int,
    val usado: Boolean,
    val expiraEn: Instant
)

interface OtpRepositoryPort {

    fun save(
        userId: UUID?,
        correo: Email,
        codigoHash: String,
        proposito: OtpPurpose,
        expiraEn: Instant
    ): StoredOtp

    fun findLatestByEmailAndProposito(
        correo: Email,
        proposito: OtpPurpose
    ): StoredOtp?

    fun markAsUsed(id: UUID)

    fun incrementAttempts(id: UUID)

    fun invalidatePrevious(correo: Email, proposito: OtpPurpose)
}