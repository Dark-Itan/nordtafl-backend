package com.nordtafl.features.auth.domain.model.value

import com.nordtafl.shared.exception.ValidationException

@JvmInline
value class Password(val value: String) {

    init {
        if (value.length < 12) {
            throw ValidationException(
                code = "INVALID_PASSWORD_FORMAT",
                message = "La contraseña debe tener al menos 12 caracteres",
                field = "password"
            )
        }

        if (value.length > 128) {
            throw ValidationException(
                code = "INVALID_PASSWORD_FORMAT",
                message = "La contraseña no puede superar los 128 caracteres",
                field = "password"
            )
        }

        if (!value.any { it.isUpperCase() }) {
            throw ValidationException(
                code = "INVALID_PASSWORD_FORMAT",
                message = "La contraseña debe contener al menos una mayúscula",
                field = "password"
            )
        }

        if (!value.any { it.isDigit() }) {
            throw ValidationException(
                code = "INVALID_PASSWORD_FORMAT",
                message = "La contraseña debe contener al menos un número",
                field = "password"
            )
        }

        if (!value.any { !it.isLetterOrDigit() }) {
            throw ValidationException(
                code = "INVALID_PASSWORD_FORMAT",
                message = "La contraseña debe contener al menos un carácter especial",
                field = "password"
            )
        }
    }
}