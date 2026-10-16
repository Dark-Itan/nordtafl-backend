package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.port.out.StoredWebAuthnCredential
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class WebAuthnCredentialMapper {

    fun toDomain(entity: WebAuthnCredentialJpaEntity): StoredWebAuthnCredential {
        return StoredWebAuthnCredential(
            id = entity.id ?: throw IllegalStateException("Credential sin ID"),
            userId = entity.userId,
            credentialId = entity.credentialId,
            publicKey = entity.publicKey,
            signCount = entity.signCount,
            transports = entity.transports?.split(",")?.filter { it.isNotBlank() } ?: emptyList(),
            createdAt = entity.createdAt ?: Instant.now(),
            lastUsedAt = entity.lastUsedAt
        )
    }

    fun toEntity(domain: StoredWebAuthnCredential): WebAuthnCredentialJpaEntity {
        return WebAuthnCredentialJpaEntity(
            id = domain.id,
            userId = domain.userId,
            credentialId = domain.credentialId,
            publicKey = domain.publicKey,
            signCount = domain.signCount,
            aaguid = null,
            transports = domain.transports.joinToString(","),
            attestationType = null,
            nombreDispositivo = null,
            createdAt = domain.createdAt,
            lastUsedAt = domain.lastUsedAt
        )
    }
}