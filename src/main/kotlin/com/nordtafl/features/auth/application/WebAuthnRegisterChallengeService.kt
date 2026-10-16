package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterChallengeCommand
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterChallengeResult
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterChallengeUseCase
import com.nordtafl.features.auth.domain.port.out.WebAuthnChallengePort
import com.nordtafl.features.auth.domain.port.out.WebAuthnCredentialRepositoryPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ConflictException
import com.nordtafl.shared.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64

@Service
class WebAuthnRegisterChallengeService(
    private val userRepository: UserRepositoryPort,
    private val webAuthnCredentialRepository: WebAuthnCredentialRepositoryPort,
    private val webAuthnChallengePort: WebAuthnChallengePort,
    @Value("\${app.webauthn.rp-id}") private val rpId: String,
    @Value("\${app.webauthn.rp-name}") private val rpName: String
) : WebAuthnRegisterChallengeUseCase {

    private val log = LoggerFactory.getLogger(WebAuthnRegisterChallengeService::class.java)
    private val secureRandom = SecureRandom()

    @Transactional(readOnly = true)
    override fun generateChallenge(
        command: WebAuthnRegisterChallengeCommand
    ): WebAuthnRegisterChallengeResult {
        log.info("Generando challenge de registro para usuario: ${command.userId}")

        val user = userRepository.findById(command.userId)
            ?: throw ResourceNotFoundException(
                resource = "Usuario",
                id = command.userId.toString()
            )

        if (webAuthnCredentialRepository.existsByUserId(command.userId)) {
            throw ConflictException(
                code = "WEBAUTHN_ALREADY_REGISTERED",
                message = "El usuario ya tiene una credencial biometrica registrada"
            )
        }

        val challenge = generateChallengeBase64()
        webAuthnChallengePort.storeRegistrationChallenge(
            userId = command.userId,
            challenge = challenge,
            ttl = Duration.ofSeconds(60)
        )

        val userIdBase64 = Base64.getUrlEncoder().withoutPadding()
            .encodeToString(command.userId.toString().toByteArray())

        return WebAuthnRegisterChallengeResult(
            challenge = challenge,
            rpId = rpId,
            rpName = rpName,
            userId = userIdBase64,
            userName = user.correo.value,
            timeout = 60000,
            attestation = "none"
        )
    }

    private fun generateChallengeBase64(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}