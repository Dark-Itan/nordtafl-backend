package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.port.out.StoredWebAuthnCredential
import com.nordtafl.features.auth.domain.port.out.WebAuthnCredentialRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class WebAuthnCredentialPersistenceAdapter(
    private val jpaRepository: WebAuthnCredentialJpaRepository,
    private val mapper: WebAuthnCredentialMapper
) : WebAuthnCredentialRepositoryPort {

    private val log = LoggerFactory.getLogger(WebAuthnCredentialPersistenceAdapter::class.java)

    override fun save(credential: StoredWebAuthnCredential): StoredWebAuthnCredential {
        log.debug("Guardando credencial WebAuthn para usuario: ${credential.userId}")

        val entity = mapper.toEntity(credential)
        val saved = jpaRepository.save(entity)

        return mapper.toDomain(saved)
    }

    override fun findByCredentialId(credentialId: String): StoredWebAuthnCredential? {
        return jpaRepository.findByCredentialId(credentialId)
            ?.let { mapper.toDomain(it) }
    }

    override fun findAllByUserId(userId: UUID): List<StoredWebAuthnCredential> {
        return jpaRepository.findAllByUserId(userId)
            .map { mapper.toDomain(it) }
    }

    override fun existsByUserId(userId: UUID): Boolean {
        return jpaRepository.existsByUserId(userId)
    }

    override fun updateSignCount(credentialId: String, newSignCount: Long) {
        log.debug("Actualizando signCount de credencial: $credentialId")

        jpaRepository.updateSignCount(credentialId, newSignCount, Instant.now())
    }
}