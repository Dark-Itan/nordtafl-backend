package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
interface UserJpaRepository : JpaRepository<UserJpaEntity, UUID> {

    fun findByCorreo(correo: String): UserJpaEntity?

    fun existsByCorreo(correo: String): Boolean
}