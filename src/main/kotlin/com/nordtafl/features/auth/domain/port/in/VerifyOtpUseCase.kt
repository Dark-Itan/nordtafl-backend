package com.nordtafl.features.auth.domain.port.`in`

data class VerifyOtpCommand(
    val correo: String,
    val codigo: String,
    val proposito: OtpPurpose
)

data class VerifyOtpResult(
    val verificado: Boolean,
    val proposito: OtpPurpose,
    val mensaje: String
)

interface VerifyOtpUseCase {

    fun verify(command: VerifyOtpCommand): VerifyOtpResult
}