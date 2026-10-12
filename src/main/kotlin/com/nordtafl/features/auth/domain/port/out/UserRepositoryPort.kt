package com.nordtafl.features.auth.domain.port.out

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import java.util.UUID

interface UserRepositoryPort {

    fun save(user: User): User

    fun findById(id: UUID): User?

    fun findByEmail(email: Email): User?

    fun existsByEmail(email: Email): Boolean

    fun update(user: User): User

    fun deleteById(id: UUID)
}