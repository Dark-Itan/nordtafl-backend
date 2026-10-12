package com.nordtafl.features.auth.domain.port.`in`

import com.nordtafl.features.auth.domain.model.User

data class WebAuthnLoginVerifyCommand(
    val credentialId: String,
    val rawId: String,
    val type: String,
    val authenticatorData: String,
    val clientDataJSON: String,
    val signature: String,
    val userHandle: String?
)

data class WebAuthnLoginVerifyResult(
    val user: User,
    val accessToken: String,
    val refreshToken: String,
    val accessTokenExpiresIn: Long
)

interface WebAuthnLoginVerifyUseCase {

    fun verifyLogin(
        command: WebAuthnLoginVerifyCommand
    ): WebAuthnLoginVerifyResult
}