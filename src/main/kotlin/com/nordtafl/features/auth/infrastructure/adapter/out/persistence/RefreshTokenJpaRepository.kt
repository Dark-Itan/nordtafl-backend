package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface RefreshTokenJpaRepository : JpaRepository<RefreshTokenJpaEntity, UUID> {

    fun findByTokenHash(tokenHash: String): RefreshTokenJpaEntity?

    @Modifying
    @Query("""
        UPDATE RefreshTokenJpaEntity r
        SET r.revoked = true,
            r.revokedAt = :now,
            r.revokedReason = :reason
        WHERE r.tokenHash = :tokenHash
          AND r.revoked = false
    """)
    fun revokeByTokenHash(
        @Param("tokenHash") tokenHash: String,
        @Param("now") now: Instant,
        @Param("reason") reason: String
    ): Int

    @Modifying
    @Query("""
        UPDATE RefreshTokenJpaEntity r
        SET r.revoked = true,
            r.revokedAt = :now,
            r.revokedReason = :reason
        WHERE r.userId = :userId
          AND r.revoked = false
    """)
    fun revokeAllByUserId(
        @Param("userId") userId: UUID,
        @Param("now") now: Instant,
        @Param("reason") reason: String
    ): Int
}