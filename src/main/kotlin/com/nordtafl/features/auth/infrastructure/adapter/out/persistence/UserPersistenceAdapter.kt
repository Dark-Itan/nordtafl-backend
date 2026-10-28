package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class UserPersistenceAdapter(
    private val jpaRepository: UserJpaRepository,
    private val mapper: UserMapper
) : UserRepositoryPort {

    private val log = LoggerFactory.getLogger(UserPersistenceAdapter::class.java)

    override fun save(user: User): User {
        log.debug("Guardando usuario en BD: ${user.correo.value}")

        val entity = mapper.toEntity(user)
        val saved = jpaRepository.save(entity)

        return mapper.toDomain(saved)
    }

    override fun findById(id: UUID): User? {
        log.debug("Buscando usuario por ID: $id")

        return jpaRepository.findById(id)
            .map { mapper.toDomain(it) }
            .orElse(null)
    }

    override fun findByEmail(email: Email): User? {
        log.debug("Buscando usuario por correo: ${email.value}")

        return jpaRepository.findByCorreo(email.value)
            ?.let { mapper.toDomain(it) }
    }

    override fun existsByEmail(email: Email): Boolean {
        log.debug("Verificando si existe correo: ${email.value}")

        return jpaRepository.existsByCorreo(email.value)
    }

    override fun update(user: User): User {
        log.debug("Actualizando usuario: ${user.id}")

        val entity = mapper.toEntity(user)
        val updated = jpaRepository.save(entity)

        return mapper.toDomain(updated)
    }

    override fun deleteById(id: UUID) {
        log.debug("Eliminando usuario: $id")

        jpaRepository.deleteById(id)
    }
}