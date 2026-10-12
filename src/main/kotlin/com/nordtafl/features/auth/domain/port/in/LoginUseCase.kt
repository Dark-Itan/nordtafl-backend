package com.nordtafl.features.auth.domain.port.`in`

import com.nordtafl.features.auth.domain.model.User

data class LoginCommand(
    val correo: String,
    val password: String
)

data class LoginResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long
)

interface LoginUseCase {

    fun login(command: LoginCommand): LoginResult
}