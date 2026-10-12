package com.nordtafl.features.auth.domain.port.`in`

data class LogoutCommand(
    val accessToken: String,
    val refreshToken: String
)

interface LogoutUseCase {

    fun logout(command: LogoutCommand)
}