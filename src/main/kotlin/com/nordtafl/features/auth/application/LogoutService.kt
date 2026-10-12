package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.port.`in`.LogoutCommand
import com.nordtafl.features.auth.domain.port.`in`.LogoutUseCase
import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.TokenBlacklistPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Duration

@Service
class LogoutService(
    private val tokenBlacklist: TokenBlacklistPort,
    private val refreshTokenRepository: RefreshTokenRepositoryPort
) : LogoutUseCase {

    private val log = LoggerFactory.getLogger(LogoutService::class.java)

    @Transactional
    override fun logout(command: LogoutCommand) {
        log.info("Cerrando sesión")

        tokenBlacklist.add(
            token = command.accessToken,
            ttl = Duration.ofMinutes(15)
        )

        val refreshTokenHash = hashToken(command.refreshToken)
        refreshTokenRepository.revoke(
            tokenHash = refreshTokenHash,
            reason = "logout"
        )

        log.info("Sesión cerrada exitosamente")
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}