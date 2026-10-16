package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.port.out.StoredRefreshToken
import org.springframework.stereotype.Component

@Component
class RefreshTokenMapper {

    fun toDomain(entity: RefreshTokenJpaEntity): StoredRefreshToken {
        return StoredRefreshToken(
            id = entity.id ?: throw IllegalStateException("RefreshToken sin ID"),
            userId = entity.userId,
            tokenHash = entity.tokenHash,
            revoked = entity.revoked,
            expiresAt = entity.expiresAt
        )
    }

    fun toEntity(domain: StoredRefreshToken): RefreshTokenJpaEntity {
        return RefreshTokenJpaEntity(
            id = domain.id,
            userId = domain.userId,
            tokenHash = domain.tokenHash,
            revoked = domain.revoked,
            revokedAt = null,
            revokedReason = null,
            expiresAt = domain.expiresAt,
            createdAt = null,
            lastUsedAt = null
        )
    }
}