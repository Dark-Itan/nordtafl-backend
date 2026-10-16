package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.port.out.PasswordResetTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.StoredPasswordResetToken
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class PasswordResetTokenPersistenceAdapter(
    private val jpaRepository: PasswordResetTokenJpaRepository,
    private val mapper: PasswordResetTokenMapper
) : PasswordResetTokenRepositoryPort {

    private val log = LoggerFactory.getLogger(PasswordResetTokenPersistenceAdapter::class.java)

    override fun save(
        userId: UUID,
        tokenHash: String,
        expiresAt: Instant
    ): StoredPasswordResetToken {
        log.debug("Guardando token de recuperación para usuario: $userId")

        val entity = PasswordResetTokenJpaEntity(
            userId = userId,
            tokenHash = tokenHash,
            usado = false,
            expiresAt = expiresAt
        )

        val saved = jpaRepository.save(entity)
        return mapper.toDomain(saved)
    }

    override fun findByHash(tokenHash: String): StoredPasswordResetToken? {
        log.debug("Buscando token de recuperación por hash")

        return jpaRepository.findByTokenHash(tokenHash)
            ?.let { mapper.toDomain(it) }
    }

    override fun markAsUsed(id: UUID) {
        log.debug("Marcando token de recuperación como usado: $id")
        jpaRepository.markAsUsed(id, Instant.now())
    }

    override fun invalidatePrevious(userId: UUID) {
        log.debug("Invalidando tokens de recuperación previos del usuario: $userId")

        val affected = jpaRepository.invalidatePrevious(userId, Instant.now())

        if (affected > 0) {
            log.info("Se invalidaron $affected tokens previos del usuario: $userId")
        }
    }
}