package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.port.`in`.RefreshTokenCommand
import com.nordtafl.features.auth.domain.port.`in`.RefreshTokenResult
import com.nordtafl.features.auth.domain.port.`in`.RefreshTokenUseCase
import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.StoredRefreshToken
import com.nordtafl.features.auth.domain.port.out.TokenGeneratorPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@Service
class RefreshTokenService(
    private val refreshTokenRepository: RefreshTokenRepositoryPort,
    private val userRepository: UserRepositoryPort,
    private val tokenGenerator: TokenGeneratorPort
) : RefreshTokenUseCase {

    private val log = LoggerFactory.getLogger(RefreshTokenService::class.java)

    @Transactional
    override fun refresh(command: RefreshTokenCommand): RefreshTokenResult {
        log.info("Refrescando token")

        val tokenHash = hashToken(command.refreshToken)

        val stored = refreshTokenRepository.findByHash(tokenHash)
            ?: throw UnauthorizedException(
                code = "REFRESH_TOKEN_INVALID",
                message = "Refresh token inválido"
            )

        if (stored.revoked) {
            log.warn("Intento de uso de refresh token revocado. Revocando todos los tokens del usuario: ${stored.userId}")
            refreshTokenRepository.revokeAllByUserId(stored.userId, "suspicion")
            throw UnauthorizedException(
                code = "REFRESH_TOKEN_INVALID",
                message = "Refresh token inválido"
            )
        }

        if (stored.expiresAt.isBefore(Instant.now())) {
            throw UnauthorizedException(
                code = "REFRESH_TOKEN_INVALID",
                message = "Refresh token expirado"
            )
        }

        val user = userRepository.findById(stored.userId)
            ?: throw UnauthorizedException(
                code = "USER_NOT_FOUND",
                message = "Usuario no encontrado"
            )

        refreshTokenRepository.revoke(tokenHash, "rotation")

        val tokens = tokenGenerator.generateTokens(user)

        val newTokenHash = hashToken(tokens.refreshToken)
        refreshTokenRepository.save(
            StoredRefreshToken(
                id = UUID.randomUUID(),
                userId = user.id!!,
                tokenHash = newTokenHash,
                revoked = false,
                expiresAt = Instant.now().plusSeconds(tokens.refreshTokenExpiresIn)
            )
        )

        log.info("Token refrescado exitosamente para usuario: ${user.id}")

        return RefreshTokenResult(
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            accessTokenExpiresIn = tokens.accessTokenExpiresIn,
            refreshTokenExpiresIn = tokens.refreshTokenExpiresIn
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}