package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.port.out.StoredPasswordResetToken
import org.springframework.stereotype.Component

@Component
class PasswordResetTokenMapper {

    fun toDomain(entity: PasswordResetTokenJpaEntity): StoredPasswordResetToken {
        return StoredPasswordResetToken(
            id = entity.id ?: throw IllegalStateException("PasswordResetToken sin ID"),
            userId = entity.userId,
            tokenHash = entity.tokenHash,
            usado = entity.usado,
            expiresAt = entity.expiresAt
        )
    }

    fun toEntity(domain: StoredPasswordResetToken): PasswordResetTokenJpaEntity {
        return PasswordResetTokenJpaEntity(
            id = domain.id,
            userId = domain.userId,
            tokenHash = domain.tokenHash,
            usado = domain.usado,
            usadoEn = null,
            expiresAt = domain.expiresAt,
            createdAt = null
        )
    }
}