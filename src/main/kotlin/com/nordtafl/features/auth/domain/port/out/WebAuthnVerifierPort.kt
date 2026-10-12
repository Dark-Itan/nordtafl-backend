package com.nordtafl.features.auth.domain.port.out

data class RegistrationVerificationResult(
    val credentialId: String,
    val publicKey: String,
    val signCount: Long,
    val aaguid: String?,
    val transports: List<String>
)

data class LoginVerificationResult(
    val credentialId: String,
    val newSignCount: Long
)

interface WebAuthnVerifierPort {

    fun verifyRegistration(
        userId: String,
        challenge: String,
        credentialId: String,
        rawId: String,
        attestationObject: String,
        clientDataJSON: String
    ): RegistrationVerificationResult

    fun verifyLogin(
        challenge: String,
        credentialId: String,
        publicKey: String,
        storedSignCount: Long,
        authenticatorData: String,
        clientDataJSON: String,
        signature: String
    ): LoginVerificationResult
}