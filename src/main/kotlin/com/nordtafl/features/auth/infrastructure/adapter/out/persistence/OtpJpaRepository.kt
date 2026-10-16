package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface OtpJpaRepository : JpaRepository<OtpJpaEntity, UUID> {

    @Query("""
        SELECT o FROM OtpJpaEntity o
        WHERE o.correo = :correo
          AND o.proposito = :proposito
          AND o.usado = false
        ORDER BY o.createdAt DESC
    """)
    fun findLatestByCorreoAndProposito(
        @Param("correo") correo: String,
        @Param("proposito") proposito: String
    ): List<OtpJpaEntity>

    @Modifying
    @Query("""
        UPDATE OtpJpaEntity o
        SET o.usado = true
        WHERE o.id = :id
    """)
    fun markAsUsed(@Param("id") id: UUID): Int

    @Modifying
    @Query("""
        UPDATE OtpJpaEntity o
        SET o.intentos = o.intentos + 1
        WHERE o.id = :id
    """)
    fun incrementAttempts(@Param("id") id: UUID): Int

    @Modifying
    @Query("""
        UPDATE OtpJpaEntity o
        SET o.usado = true
        WHERE o.correo = :correo
          AND o.proposito = :proposito
          AND o.usado = false
    """)
    fun invalidatePrevious(
        @Param("correo") correo: String,
        @Param("proposito") proposito: String
    ): Int
}