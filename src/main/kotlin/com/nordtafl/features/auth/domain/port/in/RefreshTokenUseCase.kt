package com.nordtafl.features.auth.domain.port.`in`

data class RefreshTokenCommand(
    val refreshToken: String
)

data class RefreshTokenResult(
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long,
    val refreshTokenExpiresIn: Long
)

interface RefreshTokenUseCase {

    fun refresh(command: RefreshTokenCommand): RefreshTokenResult
}