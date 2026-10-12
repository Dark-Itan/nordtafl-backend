package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterVerifyCommand
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterVerifyResult
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterVerifyUseCase
import com.nordtafl.features.auth.domain.port.out.StoredWebAuthnCredential
import com.nordtafl.features.auth.domain.port.out.WebAuthnChallengePort
import com.nordtafl.features.auth.domain.port.out.WebAuthnCredentialRepositoryPort
import com.nordtafl.features.auth.domain.port.out.WebAuthnVerifierPort
import com.nordtafl.shared.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.UUID

@Service
class WebAuthnRegisterVerifyService(
    private val webAuthnCredentialRepository: WebAuthnCredentialRepositoryPort,
    private val webAuthnChallengePort: WebAuthnChallengePort,
    private val webAuthnVerifier: WebAuthnVerifierPort
) : WebAuthnRegisterVerifyUseCase {

    private val log = LoggerFactory.getLogger(WebAuthnRegisterVerifyService::class.java)

    @Transactional
    override fun verifyRegistration(
        command: WebAuthnRegisterVerifyCommand
    ): WebAuthnRegisterVerifyResult {
        log.info("Verificando registro WebAuthn para usuario: ${command.userId}")

        val challenge = webAuthnChallengePort.getRegistrationChallenge(command.userId)
            ?: throw ValidationException(
                code = "WEBAUTHN_CHALLENGE_EXPIRED",
                message = "El challenge ha expirado. Solicita uno nuevo.",
                field = "challenge"
            )

        val verificationResult = webAuthnVerifier.verifyRegistration(
            userId = command.userId.toString(),
            challenge = challenge,
            credentialId = command.credentialId,
            rawId = command.rawId,
            attestationObject = command.attestationObject,
            clientDataJSON = command.clientDataJSON
        )

        val credential = StoredWebAuthnCredential(
            id = UUID.randomUUID(),
            userId = command.userId,
            credentialId = verificationResult.credentialId,
            publicKey = verificationResult.publicKey,
            signCount = verificationResult.signCount,
            transports = verificationResult.transports,
            createdAt = Instant.now(),
            lastUsedAt = null
        )

        webAuthnCredentialRepository.save(credential)
        webAuthnChallengePort.deleteRegistrationChallenge(command.userId)

        log.info("Credencial WebAuthn registrada para usuario: ${command.userId}")

        return WebAuthnRegisterVerifyResult(
            credentialId = verificationResult.credentialId,
            registradoEn = Instant.now(),
            mensaje = "Credencial biométrica registrada correctamente."
        )
    }
}