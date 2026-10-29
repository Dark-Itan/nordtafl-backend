package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.port.`in`.LogoutCommand
import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.TokenBlacklistPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Duration
import java.security.MessageDigest

@DisplayName("LogoutService")
class LogoutServiceTest {

    private lateinit var tokenBlacklist: TokenBlacklistPort
    private lateinit var refreshTokenRepository: RefreshTokenRepositoryPort
    private lateinit var service: LogoutService

    private val accessToken = "access_token_ejemplo"
    private val refreshToken = "refresh_token_ejemplo"

    @BeforeEach
    fun setUp() {
        tokenBlacklist = mockk(relaxed = true)
        refreshTokenRepository = mockk(relaxed = true)
        service = LogoutService(tokenBlacklist, refreshTokenRepository)
    }

    // Helper para calcular el hash SHA-256 (igual que en el servicio)
    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    @Nested
    @DisplayName("Casos exitosos")
    inner class SuccessCases {

        @Test
        fun `debe hacer logout exitosamente invalidando tokens`() {
            val expectedHash = hashToken(refreshToken)

            // When
            service.logout(LogoutCommand(accessToken, refreshToken))

            // Then
            // 1. Verifica que el access token se agregó a la blacklist con TTL de 15 min
            verify(exactly = 1) {
                tokenBlacklist.add(accessToken, Duration.ofMinutes(15))
            }

            // 2. Verifica que el refresh token se revocó en la BD
            verify(exactly = 1) {
                refreshTokenRepository.revoke(expectedHash, "logout")
            }
        }
    }
}