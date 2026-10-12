package com.nordtafl.features.auth.domain.port.out

import com.nordtafl.features.auth.domain.model.User
import java.util.UUID

data class GeneratedTokens(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long
)

interface TokenGeneratorPort {

    fun generateTokens(user: User): GeneratedTokens

    fun validateAccessToken(token: String): UUID?

    fun extractUserId(token: String): UUID?
}