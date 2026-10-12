package com.nordtafl.features.auth.domain.port.`in`

data class RequestOtpCommand(
    val correo: String,
    val proposito: OtpPurpose
)

data class RequestOtpResult(
    val mensaje: String,
    val expiraEn: Long
)

enum class OtpPurpose {
    VERIFICACION_CUENTA,
    RECUPERACION_PASSWORD,
    ACCION_SENSIBLE
}

interface RequestOtpUseCase {

    fun request(command: RequestOtpCommand): RequestOtpResult
}