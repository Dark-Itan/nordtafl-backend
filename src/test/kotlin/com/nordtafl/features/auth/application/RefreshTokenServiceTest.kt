package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.RefreshTokenCommand
import com.nordtafl.features.auth.domain.port.out.GeneratedTokens
import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.StoredRefreshToken
import com.nordtafl.features.auth.domain.port.out.TokenGeneratorPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.UnauthorizedException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@DisplayName("RefreshTokenService")
class RefreshTokenServiceTest {

    private lateinit var refreshTokenRepository: RefreshTokenRepositoryPort
    private lateinit var userRepository: UserRepositoryPort
    private lateinit var tokenGenerator: TokenGeneratorPort
    private lateinit var service: RefreshTokenService

    private val userId = UUID.randomUUID()
    private val email = Email("mariana@ejemplo.com")
    private val refreshToken = "refresh_token_valido"

    @BeforeEach
    fun setUp() {
        refreshTokenRepository = mockk(relaxed = true)
        userRepository = mockk(relaxed = true)
        tokenGenerator = mockk(relaxed = true)
        service = RefreshTokenService(refreshTokenRepository, userRepository, tokenGenerator)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    private fun buildUser(): User = User(
        id = userId,
        nombre = "Mariana",
        correo = email,
        passwordHash = "hashed_password",
        rol = UserRole.JUGADOR,
        estado = UserStatus.ACTIVO,
        emailVerificado = true
    )

    private fun buildStoredToken(
        revoked: Boolean = false,
        expirado: Boolean = false
    ): StoredRefreshToken = StoredRefreshToken(
        id = UUID.randomUUID(),
        userId = userId,
        tokenHash = hashToken(refreshToken),
        revoked = revoked,
        expiresAt = if (expirado) Instant.now().minusSeconds(3600) else Instant.now().plusSeconds(3600)
    )

    @Nested
    @DisplayName("Casos exitosos")
    inner class SuccessCases {

        @Test
        fun `debe refrescar tokens exitosamente (rotacion)`() {
            val storedToken = buildStoredToken()
            val user = buildUser()

            every { refreshTokenRepository.findByHash(any()) } returns storedToken
            every { userRepository.findById(userId) } returns user
            every { tokenGenerator.generateTokens(user) } returns GeneratedTokens(
                accessToken = "nuevo_access",
                refreshToken = "nuevo_refresh",
                accessTokenExpiresIn = 900,
                refreshTokenExpiresIn = 604800
            )

            val result = service.refresh(RefreshTokenCommand(refreshToken))

            assertThat(result.accessToken).isEqualTo("nuevo_access")
            assertThat(result.refreshToken).isEqualTo("nuevo_refresh")

            // Verifica que el token viejo fue revocado
            verify(exactly = 1) {
                refreshTokenRepository.revoke(hashToken(refreshToken), "rotation")
            }
            // Verifica que el nuevo token fue guardado
            verify(exactly = 1) {
                refreshTokenRepository.save(any())
            }
        }
    }

    @Nested
    @DisplayName("Casos de error")
    inner class ErrorCases {

        @Test
        fun `debe lanzar UnauthorizedException si el token no existe`() {
            every { refreshTokenRepository.findByHash(any()) } returns null

            assertThatThrownBy {
                service.refresh(RefreshTokenCommand(refreshToken))
            }.isInstanceOf(UnauthorizedException::class.java)
                .hasMessageContaining("Refresh token inválido")
        }

        @Test
        fun `debe lanzar UnauthorizedException si el token fue revocado (replay attack)`() {
            val revokedToken = buildStoredToken(revoked = true)
            every { refreshTokenRepository.findByHash(any()) } returns revokedToken

            assertThatThrownBy {
                service.refresh(RefreshTokenCommand(refreshToken))
            }.isInstanceOf(UnauthorizedException::class.java)
                .hasMessageContaining("Refresh token inválido")

            // Verifica la seguridad: revoca TODOS los tokens del usuario por sospecha
            verify(exactly = 1) {
                refreshTokenRepository.revokeAllByUserId(userId, "suspicion")
            }
        }

        @Test
        fun `debe lanzar UnauthorizedException si el token expiro`() {
            val expiredToken = buildStoredToken(expirado = true)
            every { refreshTokenRepository.findByHash(any()) } returns expiredToken

            assertThatThrownBy {
                service.refresh(RefreshTokenCommand(refreshToken))
            }.isInstanceOf(UnauthorizedException::class.java)
                .hasMessageContaining("Refresh token expirado")
        }

        @Test
        fun `debe lanzar UnauthorizedException si el usuario no existe`() {
            val storedToken = buildStoredToken()
            every { refreshTokenRepository.findByHash(any()) } returns storedToken
            every { userRepository.findById(userId) } returns null

            assertThatThrownBy {
                service.refresh(RefreshTokenCommand(refreshToken))
            }.isInstanceOf(UnauthorizedException::class.java)
                .hasMessageContaining("Usuario no encontrado")
        }
    }
}