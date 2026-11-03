package com.nordtafl.features.auth.infrastructure.adapter.`in`.rest.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RegisterRequest(
    @field:NotBlank(message = "El nombre es obligatorio")
    @field:Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @field:Pattern(
        regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ\\s]+$",
        message = "El nombre solo puede contener letras y espacios"
    )
    val nombre: String,

    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato valido")
    @field:Size(max = 254, message = "El correo no puede superar los 254 caracteres")
    val correo: String,

    @field:NotBlank(message = "La contrasena es obligatoria")
    @field:Size(min = 8, max = 128, message = "La contrasena debe tener entre 8 y 128 caracteres")
    @field:Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#]).+$",
        message = "La contrasena debe tener al menos una mayuscula, una minuscula, un numero y un simbolo (!, @, #)"
    )
    val password: String,

    @field:NotBlank(message = "El rol es obligatorio")
    @field:Pattern(
        regexp = "^(jugador|admin)$",
        message = "El rol debe ser 'jugador' o 'admin'"
    )
    val rol: String = "jugador"
)

data class LoginRequest(
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato valido")
    val correo: String,

    @field:NotBlank(message = "La contraseña es obligatoria")
    val password: String
)

data class LogoutRequest(
    @field:NotBlank(message = "El refresh token es obligatorio")
    val refreshToken: String
)

data class RequestOtpRequest(
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato válido")
    val correo: String,

    @field:NotBlank(message = "El propósito es obligatorio")
    val proposito: String
)

data class VerifyOtpRequest(
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato válido")
    val correo: String,

    @field:NotBlank(message = "El código es obligatorio")
    @field:Size(min = 6, max = 6, message = "El código debe tener 6 dígitos")
    val codigo: String,

    @field:NotBlank(message = "El propósito es obligatorio")
    val proposito: String
)

data class RefreshTokenRequest(
    @field:NotBlank(message = "El refresh token es obligatorio")
    val refreshToken: String
)

data class ForgotPasswordRequest(
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato válido")
    val correo: String
)

data class ResetPasswordRequest(
    @field:NotBlank(message = "El correo es obligatorio")
    @field:Email(message = "El correo no tiene un formato válido")
    val correo: String,

    @field:NotBlank(message = "El código es obligatorio")
    val codigo: String,

    @field:NotBlank(message = "La nueva contraseña es obligatoria")
    @field:Size(min = 12, max = 128, message = "La contraseña debe tener entre 12 y 128 caracteres")
    val nuevaPassword: String
)

data class GoogleLoginRequest(
    @field:NotBlank(message = "El ID Token es obligatorio")
    val idToken: String
)