package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.time.Instant
import java.util.UUID

@Repository
interface WebAuthnCredentialJpaRepository :
    JpaRepository<WebAuthnCredentialJpaEntity, UUID> {

    fun findByCredentialId(credentialId: String): WebAuthnCredentialJpaEntity?

    fun findAllByUserId(userId: UUID): List<WebAuthnCredentialJpaEntity>

    fun existsByUserId(userId: UUID): Boolean

    @Modifying
    @Query("""
        UPDATE WebAuthnCredentialJpaEntity c
        SET c.signCount = :newSignCount,
            c.lastUsedAt = :now
        WHERE c.credentialId = :credentialId
    """)
    fun updateSignCount(
        @Param("credentialId") credentialId: String,
        @Param("newSignCount") newSignCount: Long,
        @Param("now") now: Instant
    ): Int
}