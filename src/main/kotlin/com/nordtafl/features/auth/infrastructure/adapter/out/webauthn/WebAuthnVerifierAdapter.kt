package com.nordtafl.features.auth.infrastructure.adapter.out.webauthn

import com.nordtafl.features.auth.domain.port.out.LoginVerificationResult
import com.nordtafl.features.auth.domain.port.out.RegistrationVerificationResult
import com.nordtafl.features.auth.domain.port.out.WebAuthnVerifierPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class WebAuthnVerifierAdapter : WebAuthnVerifierPort {

    private val log = LoggerFactory.getLogger(WebAuthnVerifierAdapter::class.java)

    override fun verifyRegistration(
        userId: String,
        challenge: String,
        credentialId: String,
        rawId: String,
        attestationObject: String,
        clientDataJSON: String
    ): RegistrationVerificationResult {
        log.warn("WebAuthn verificación de registro NO IMPLEMENTADA todavía")
        throw UnsupportedOperationException(
            "WebAuthn aún no implementado. Se implementará al conectar el frontend Android."
        )
    }

    override fun verifyLogin(
        challenge: String,
        credentialId: String,
        publicKey: String,
        storedSignCount: Long,
        authenticatorData: String,
        clientDataJSON: String,
        signature: String
    ): LoginVerificationResult {
        log.warn("WebAuthn verificación de login NO IMPLEMENTADA todavía")
        throw UnsupportedOperationException(
            "WebAuthn aún no implementado. Se implementará al conectar el frontend Android."
        )
    }
}