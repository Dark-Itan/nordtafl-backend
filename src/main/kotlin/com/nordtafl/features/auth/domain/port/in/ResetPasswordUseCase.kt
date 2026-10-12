package com.nordtafl.features.auth.domain.port.`in`

data class ResetPasswordCommand(
    val correo: String,
    val codigo: String,
    val nuevaPassword: String
)

data class ResetPasswordResult(
    val mensaje: String
)

interface ResetPasswordUseCase {

    fun resetPassword(command: ResetPasswordCommand): ResetPasswordResult
}