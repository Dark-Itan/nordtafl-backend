package com.nordtafl.features.auth.domain.port.`in`

import com.nordtafl.features.auth.domain.model.User

data class GoogleLoginCommand(
    val idToken: String
)

data class GoogleLoginResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val isNewUser: Boolean
)

interface GoogleLoginUseCase {

    fun loginWithGoogle(command: GoogleLoginCommand): GoogleLoginResult
}