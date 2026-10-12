package com.nordtafl.features.auth.domain.port.`in`

import java.util.UUID

data class WebAuthnRegisterVerifyCommand(
    val userId: UUID,
    val credentialId: String,
    val rawId: String,
    val type: String,
    val attestationObject: String,
    val clientDataJSON: String
)

data class WebAuthnRegisterVerifyResult(
    val credentialId: String,
    val registradoEn: java.time.Instant,
    val mensaje: String
)

interface WebAuthnRegisterVerifyUseCase {

    fun verifyRegistration(
        command: WebAuthnRegisterVerifyCommand
    ): WebAuthnRegisterVerifyResult
}