package com.nordtafl.features.auth.infrastructure.adapter.`in`.rest.dto

import java.time.Instant

data class UserResponse(
    val id: String,
    val nombre: String,
    val correo: String,
    val rol: String,
    val estado: String,
    val emailVerificado: Boolean,
    val avatarUrl: String?,
    val ultimoAcceso: Instant?,
    val creadoEn: Instant?
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String,
    val user: UserResponse
)

data class GoogleLoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String,
    val user: UserResponse,
    val isNewUser: Boolean
)

data class RefreshTokenResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val tokenType: String
)

data class RequestOtpResponse(
    val mensaje: String,
    val expiraEn: Long
)

data class VerifyOtpResponse(
    val verificado: Boolean,
    val proposito: String,
    val mensaje: String
)

data class ForgotPasswordResponse(
    val mensaje: String,
    val expiraEn: Long
)

data class ResetPasswordResponse(
    val mensaje: String
)

data class UploadAvatarResponse(
    val avatarUrl: String,
    val mensaje: String
)