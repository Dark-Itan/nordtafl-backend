package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.StoredRefreshToken
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class RefreshTokenPersistenceAdapter(
    private val jpaRepository: RefreshTokenJpaRepository,
    private val mapper: RefreshTokenMapper
) : RefreshTokenRepositoryPort {

    private val log = LoggerFactory.getLogger(RefreshTokenPersistenceAdapter::class.java)

    override fun save(token: StoredRefreshToken): StoredRefreshToken {
        log.debug("Guardando refresh token para usuario: ${token.userId}")

        val entity = mapper.toEntity(token)
        val saved = jpaRepository.save(entity)

        return mapper.toDomain(saved)
    }

    override fun findByHash(tokenHash: String): StoredRefreshToken? {
        log.debug("Buscando refresh token por hash")

        return jpaRepository.findByTokenHash(tokenHash)
            ?.let { mapper.toDomain(it) }
    }

    override fun revoke(tokenHash: String, reason: String) {
        log.debug("Revocando refresh token. Razón: $reason")

        val affected = jpaRepository.revokeByTokenHash(
            tokenHash = tokenHash,
            now = Instant.now(),
            reason = reason
        )

        if (affected == 0) {
            log.warn("No se revocó ningún token con hash proporcionado")
        }
    }

    override fun revokeAllByUserId(userId: UUID, reason: String) {
        log.warn("Revocando TODOS los refresh tokens del usuario: $userId. Razón: $reason")

        val affected = jpaRepository.revokeAllByUserId(
            userId = userId,
            now = Instant.now(),
            reason = reason
        )

        log.info("Se revocaron $affected tokens del usuario: $userId")
    }
}