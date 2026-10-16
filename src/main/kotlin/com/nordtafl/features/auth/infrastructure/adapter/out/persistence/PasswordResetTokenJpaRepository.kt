package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface PasswordResetTokenJpaRepository :
    JpaRepository<PasswordResetTokenJpaEntity, UUID> {

    fun findByTokenHash(tokenHash: String): PasswordResetTokenJpaEntity?

    @Modifying
    @Query("""
        UPDATE PasswordResetTokenJpaEntity t
        SET t.usado = true,
            t.usadoEn = :now
        WHERE t.id = :id
    """)
    fun markAsUsed(
        @Param("id") id: UUID,
        @Param("now") now: Instant
    ): Int

    @Modifying
    @Query("""
        UPDATE PasswordResetTokenJpaEntity t
        SET t.usado = true,
            t.usadoEn = :now
        WHERE t.userId = :userId
          AND t.usado = false
    """)
    fun invalidatePrevious(
        @Param("userId") userId: UUID,
        @Param("now") now: Instant
    ): Int
}