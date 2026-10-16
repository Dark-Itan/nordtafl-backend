package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.`in`.OtpPurpose
import com.nordtafl.features.auth.domain.port.out.OtpRepositoryPort
import com.nordtafl.features.auth.domain.port.out.StoredOtp
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class OtpPersistenceAdapter(
    private val jpaRepository: OtpJpaRepository,
    private val mapper: OtpMapper
) : OtpRepositoryPort {

    private val log = LoggerFactory.getLogger(OtpPersistenceAdapter::class.java)

    override fun save(
        userId: UUID?,
        correo: Email,
        codigoHash: String,
        proposito: OtpPurpose,
        expiraEn: Instant
    ): StoredOtp {
        log.debug("Guardando OTP para correo: ${correo.value}")

        val entity = OtpJpaEntity(
            userId = userId,
            correo = correo.value,
            codigoHash = codigoHash,
            proposito = proposito.name,
            intentos = 0,
            maxIntentos = 5,
            usado = false,
            expiraEn = expiraEn
        )

        val saved = jpaRepository.save(entity)
        return mapper.toDomain(saved)
    }

    override fun findLatestByEmailAndProposito(
        correo: Email,
        proposito: OtpPurpose
    ): StoredOtp? {
        log.debug("Buscando OTP más reciente para correo: ${correo.value}")

        return jpaRepository.findLatestByCorreoAndProposito(
            correo = correo.value,
            proposito = proposito.name
        ).firstOrNull()?.let { mapper.toDomain(it) }
    }

    override fun markAsUsed(id: UUID) {
        log.debug("Marcando OTP como usado: $id")
        jpaRepository.markAsUsed(id)
    }

    override fun incrementAttempts(id: UUID) {
        log.debug("Incrementando intentos de OTP: $id")
        jpaRepository.incrementAttempts(id)
    }

    override fun invalidatePrevious(correo: Email, proposito: OtpPurpose) {
        log.debug("Invalidando OTPs previos para correo: ${correo.value}")

        val affected = jpaRepository.invalidatePrevious(
            correo = correo.value,
            proposito = proposito.name
        )

        if (affected > 0) {
            log.info("Se invalidaron $affected OTPs previos")
        }
    }
}