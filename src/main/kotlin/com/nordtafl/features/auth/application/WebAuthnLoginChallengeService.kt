package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnCredentialDescriptor
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnLoginChallengeCommand
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnLoginChallengeResult
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnLoginChallengeUseCase
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.features.auth.domain.port.out.WebAuthnChallengePort
import com.nordtafl.features.auth.domain.port.out.WebAuthnCredentialRepositoryPort
import com.nordtafl.shared.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.SecureRandom
import java.time.Duration
import java.util.Base64

@Service
class WebAuthnLoginChallengeService(
    private val userRepository: UserRepositoryPort,
    private val webAuthnCredentialRepository: WebAuthnCredentialRepositoryPort,
    private val webAuthnChallengePort: WebAuthnChallengePort,
    @Value("\${app.webauthn.rp-id}") private val rpId: String
) : WebAuthnLoginChallengeUseCase {

    private val log = LoggerFactory.getLogger(WebAuthnLoginChallengeService::class.java)
    private val secureRandom = SecureRandom()

    @Transactional(readOnly = true)
    override fun generateChallenge(
        command: WebAuthnLoginChallengeCommand
    ): WebAuthnLoginChallengeResult {
        log.info("Generando challenge de login para correo: ${command.correo}")

        val email = Email(command.correo)
        val user = userRepository.findByEmail(email)
            ?: throw ResourceNotFoundException(
                resource = "Usuario",
                id = command.correo
            )

        val credentials = webAuthnCredentialRepository.findAllByUserId(user.id!!)
        if (credentials.isEmpty()) {
            throw ResourceNotFoundException(
                resource = "Credenciales WebAuthn",
                id = user.id.toString()
            )
        }

        val challenge = generateChallengeBase64()
        webAuthnChallengePort.storeLoginChallenge(
            correo = command.correo,
            challenge = challenge,
            ttl = Duration.ofSeconds(60)
        )

        val descriptors = credentials.map {
            WebAuthnCredentialDescriptor(
                id = it.credentialId,
                type = "public-key"
            )
        }

        return WebAuthnLoginChallengeResult(
            challenge = challenge,
            rpId = rpId,
            allowCredentials = descriptors,
            timeout = 60000,
            userVerification = "preferred"
        )
    }

    private fun generateChallengeBase64(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }
}