package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.port.`in`.WebAuthnLoginVerifyCommand
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnLoginVerifyResult
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnLoginVerifyUseCase
import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.StoredRefreshToken
import com.nordtafl.features.auth.domain.port.out.TokenGeneratorPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.features.auth.domain.port.out.WebAuthnChallengePort
import com.nordtafl.features.auth.domain.port.out.WebAuthnCredentialRepositoryPort
import com.nordtafl.features.auth.domain.port.out.WebAuthnVerifierPort
import com.nordtafl.shared.exception.UnauthorizedException
import com.nordtafl.shared.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@Service
class WebAuthnLoginVerifyService(
    private val webAuthnCredentialRepository: WebAuthnCredentialRepositoryPort,
    private val webAuthnChallengePort: WebAuthnChallengePort,
    private val webAuthnVerifier: WebAuthnVerifierPort,
    private val userRepository: UserRepositoryPort,
    private val tokenGenerator: TokenGeneratorPort,
    private val refreshTokenRepository: RefreshTokenRepositoryPort
) : WebAuthnLoginVerifyUseCase {

    private val log = LoggerFactory.getLogger(WebAuthnLoginVerifyService::class.java)

    @Transactional
    override fun verifyLogin(
        command: WebAuthnLoginVerifyCommand
    ): WebAuthnLoginVerifyResult {
        log.info("Verificando login WebAuthn con credentialId: ${command.credentialId}")

        val credential = webAuthnCredentialRepository.findByCredentialId(command.credentialId)
            ?: throw UnauthorizedException(
                code = "WEBAUTHN_CREDENTIAL_NOT_FOUND",
                message = "Credencial biométrica no encontrada"
            )

        val user = userRepository.findById(credential.userId)
            ?: throw UnauthorizedException(
                code = "USER_NOT_FOUND",
                message = "Usuario no encontrado"
            )

        val challenge = webAuthnChallengePort.getLoginChallenge(user.correo.value)
            ?: throw ValidationException(
                code = "WEBAUTHN_CHALLENGE_EXPIRED",
                message = "El challenge ha expirado. Solicita uno nuevo.",
                field = "challenge"
            )

        val verificationResult = webAuthnVerifier.verifyLogin(
            challenge = challenge,
            credentialId = command.credentialId,
            publicKey = credential.publicKey,
            storedSignCount = credential.signCount,
            authenticatorData = command.authenticatorData,
            clientDataJSON = command.clientDataJSON,
            signature = command.signature
        )

        webAuthnCredentialRepository.updateSignCount(
            credentialId = command.credentialId,
            newSignCount = verificationResult.newSignCount
        )

        webAuthnChallengePort.deleteLoginChallenge(user.correo.value)

        val tokens = tokenGenerator.generateTokens(user)

        val refreshTokenHash = hashToken(tokens.refreshToken)
        refreshTokenRepository.save(
            StoredRefreshToken(
                id = UUID.randomUUID(),
                userId = user.id!!,
                tokenHash = refreshTokenHash,
                revoked = false,
                expiresAt = Instant.now().plusSeconds(tokens.refreshTokenExpiresIn)
            )
        )

        log.info("Login WebAuthn exitoso para usuario: ${user.id}")

        return WebAuthnLoginVerifyResult(
            user = user,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            accessTokenExpiresIn = tokens.accessTokenExpiresIn
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}