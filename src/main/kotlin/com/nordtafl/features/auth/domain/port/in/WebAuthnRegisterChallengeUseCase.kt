package com.nordtafl.features.auth.domain.port.`in`

import java.util.UUID

data class WebAuthnRegisterChallengeCommand(
    val userId: UUID
)

data class WebAuthnRegisterChallengeResult(
    val challenge: String,
    val rpId: String,
    val rpName: String,
    val userId: String,
    val userName: String,
    val timeout: Long,
    val attestation: String
)

interface WebAuthnRegisterChallengeUseCase {

    fun generateChallenge(
        command: WebAuthnRegisterChallengeCommand
    ): WebAuthnRegisterChallengeResult
}