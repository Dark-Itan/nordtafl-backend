package com.nordtafl.features.auth.domain.model.value

import com.nordtafl.shared.exception.ValidationException

@JvmInline
value class Email(val value: String) {

    init {
        val normalized = value.trim().lowercase()

        if (normalized.isBlank()) {
            throw ValidationException(
                code = "INVALID_EMAIL",
                message = "El correo no puede estar vacío",
                field = "correo"
            )
        }

        if (normalized.length > 254) {
            throw ValidationException(
                code = "INVALID_EMAIL",
                message = "El correo no puede superar los 254 caracteres",
                field = "correo"
            )
        }

        if (!normalized.matches(EMAIL_REGEX)) {
            throw ValidationException(
                code = "INVALID_EMAIL",
                message = "El correo '$value' no tiene un formato válido",
                field = "correo"
            )
        }
    }

    fun normalized(): String = value.trim().lowercase()

    companion object {
        private val EMAIL_REGEX = Regex(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
        )
    }
}