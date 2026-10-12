package com.nordtafl.features.auth.domain.port.`in`

data class ForgotPasswordCommand(
    val correo: String
)

data class ForgotPasswordResult(
    val mensaje: String,
    val expiraEn: Long
)

interface ForgotPasswordUseCase {

    fun forgotPassword(command: ForgotPasswordCommand): ForgotPasswordResult
}