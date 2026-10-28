package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import org.springframework.stereotype.Component

@Component
class UserMapper {

    fun toDomain(entity: UserJpaEntity): User {
        return User(
            id = entity.id,
            nombre = entity.nombre,
            correo = Email(entity.correo),
            passwordHash = entity.passwordHash,
            rol = parseRole(entity.rol),
            estado = parseStatus(entity.estado),
            emailVerificado = entity.emailVerificado,
            avatarUrl = entity.avatarUrl,
            googleId = entity.googleId,
            ultimoAcceso = entity.ultimoAcceso,
            createdAt = entity.createdAt,
            updatedAt = entity.updatedAt
        )
    }

    fun toEntity(domain: User): UserJpaEntity {
        return UserJpaEntity(
            id = domain.id,
            nombre = domain.nombre,
            correo = domain.correo.value,
            passwordHash = domain.passwordHash,
            rol = domain.rol.name.lowercase(),
            estado = domain.estado.name.lowercase(),
            emailVerificado = domain.emailVerificado,
            avatarUrl = domain.avatarUrl,
            googleId = domain.googleId,
            ultimoAcceso = domain.ultimoAcceso,
            createdAt = domain.createdAt,
            updatedAt = domain.updatedAt
        )
    }

    private fun parseRole(rol: String): UserRole {
        return when (rol.lowercase()) {
            "jugador" -> UserRole.JUGADOR
            "admin" -> UserRole.ADMIN
            else -> UserRole.JUGADOR
        }
    }

    private fun parseStatus(status: String): UserStatus {
        return when (status.lowercase()) {
            "pendiente_verificacion" -> UserStatus.PENDIENTE_VERIFICACION
            "activo" -> UserStatus.ACTIVO
            "bloqueado" -> UserStatus.BLOQUEADO
            "eliminado" -> UserStatus.ELIMINADO
            else -> UserStatus.PENDIENTE_VERIFICACION
        }
    }
}