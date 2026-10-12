package com.nordtafl.features.auth.domain.port.`in`

data class WebAuthnLoginChallengeCommand(
    val correo: String
)

data class WebAuthnLoginChallengeResult(
    val challenge: String,
    val rpId: String,
    val allowCredentials: List<WebAuthnCredentialDescriptor>,
    val timeout: Long,
    val userVerification: String
)

data class WebAuthnCredentialDescriptor(
    val id: String,
    val type: String
)

interface WebAuthnLoginChallengeUseCase {

    fun generateChallenge(
        command: WebAuthnLoginChallengeCommand
    ): WebAuthnLoginChallengeResult
}