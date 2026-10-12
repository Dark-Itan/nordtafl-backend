package com.nordtafl.features.auth.domain.model

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.shared.exception.ValidationException
import java.time.Instant
import java.util.UUID

data class User(
    val id: UUID?,
    val nombre: String,
    val correo: Email,
    val passwordHash: String,
    val rol: UserRole,
    val estado: UserStatus,
    val emailVerificado: Boolean,
    val avatarUrl: String? = null,
    val googleId: String? = null,
    val ultimoAcceso: Instant? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
) {

    init {
        if (nombre.isBlank()) {
            throw ValidationException(
                code = "INVALID_NAME",
                message = "El nombre no puede estar vacío",
                field = "nombre"
            )
        }

        if (nombre.length < 2 || nombre.length > 100) {
            throw ValidationException(
                code = "INVALID_NAME",
                message = "El nombre debe tener entre 2 y 100 caracteres",
                field = "nombre"
            )
        }
    }

    fun isActive(): Boolean = estado == UserStatus.ACTIVO

    fun isVerified(): Boolean = emailVerificado

    fun hasBiometricCredentials(): Boolean = false

    fun isGoogleAccount(): Boolean = googleId != null
}