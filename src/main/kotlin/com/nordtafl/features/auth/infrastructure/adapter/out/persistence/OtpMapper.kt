package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.`in`.OtpPurpose
import com.nordtafl.features.auth.domain.port.out.StoredOtp
import org.springframework.stereotype.Component

@Component
class OtpMapper {

    fun toDomain(entity: OtpJpaEntity): StoredOtp {
        return StoredOtp(
            id = entity.id ?: throw IllegalStateException("OTP sin ID"),
            userId = entity.userId,
            correo = Email(entity.correo),
            codigoHash = entity.codigoHash,
            proposito = parsePurpose(entity.proposito),
            intentos = entity.intentos,
            maxIntentos = entity.maxIntentos,
            usado = entity.usado,
            expiraEn = entity.expiraEn
        )
    }

    fun toEntity(domain: StoredOtp): OtpJpaEntity {
        return OtpJpaEntity(
            id = domain.id,
            userId = domain.userId,
            correo = domain.correo.value,
            codigoHash = domain.codigoHash,
            proposito = domain.proposito.name,
            intentos = domain.intentos,
            maxIntentos = domain.maxIntentos,
            usado = domain.usado,
            expiraEn = domain.expiraEn,
            createdAt = null
        )
    }

    private fun parsePurpose(purpose: String): OtpPurpose {
        return when (purpose.uppercase()) {
            "VERIFICACION_CUENTA" -> OtpPurpose.VERIFICACION_CUENTA
            "RECUPERACION_PASSWORD" -> OtpPurpose.RECUPERACION_PASSWORD
            "ACCION_SENSIBLE" -> OtpPurpose.ACCION_SENSIBLE
            else -> OtpPurpose.VERIFICACION_CUENTA
        }
    }
}