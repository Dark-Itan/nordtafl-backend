package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.Password
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.ResetPasswordCommand
import com.nordtafl.features.auth.domain.port.out.PasswordHasherPort
import com.nordtafl.features.auth.domain.port.out.PasswordResetTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.StoredPasswordResetToken
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ValidationException
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

@DisplayName("ResetPasswordService")
class ResetPasswordServiceTest {

    private lateinit var userRepository: UserRepositoryPort
    private lateinit var passwordHasher: PasswordHasherPort
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepositoryPort
    private lateinit var refreshTokenRepository: RefreshTokenRepositoryPort
    private lateinit var service: ResetPasswordService

    private val userId = UUID.randomUUID()
    private val email = Email("mariana@ejemplo.com")
    private val codigo = "ABC123XYZ"
    private val nuevaPasswordStr = "NuevaPassword123!"
    private val nuevaPassword = Password(nuevaPasswordStr)

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        passwordHasher = mockk(relaxed = true)
        passwordResetTokenRepository = mockk(relaxed = true)
        refreshTokenRepository = mockk(relaxed = true)
        service = ResetPasswordService(
            userRepository,
            passwordHasher,
            passwordResetTokenRepository,
            refreshTokenRepository
        )
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
        passwordHash = "viejo_hash",
        rol = UserRole.JUGADOR,
        estado = UserStatus.ACTIVO,
        emailVerificado = true
    )

    private fun buildStoredToken(
        usado: Boolean = false,
        expirado: Boolean = false
    ): StoredPasswordResetToken = StoredPasswordResetToken(
        id = UUID.randomUUID(),
        userId = userId,
        tokenHash = hashToken(codigo),
        usado = usado,
        expiresAt = if (expirado) Instant.now().minusSeconds(3600) else Instant.now().plusSeconds(600)
    )

    @Nested
    @DisplayName("Casos exitosos")
    inner class SuccessCases {

        @Test
        fun `debe resetear password exitosamente y revocar sesiones`() {
            val user = buildUser()
            val token = buildStoredToken()

            every { passwordResetTokenRepository.findByHash(any()) } returns token
            every { userRepository.findById(userId) } returns user
            every { passwordHasher.hash(nuevaPassword) } returns "nuevo_hash"

            val result = service.resetPassword(
                ResetPasswordCommand(email.value, codigo, nuevaPasswordStr)
            )

            assertThat(result.mensaje).contains("restablecida correctamente")

            // Verifica que actualizó el usuario
            verify(exactly = 1) { userRepository.update(match { it.passwordHash == "nuevo_hash" }) }

            // Verifica que el token fue marcado como usado
            verify(exactly = 1) { passwordResetTokenRepository.markAsUsed(token.id) }

            // Verifica que revocó TODAS las sesiones (seguridad)
            verify(exactly = 1) { refreshTokenRepository.revokeAllByUserId(userId, "password_reset") }
        }
    }

    @Nested
    @DisplayName("Casos de error")
    inner class ErrorCases {

        @Test
        fun `debe lanzar ValidationException si el token no existe`() {
            every { passwordResetTokenRepository.findByHash(any()) } returns null

            assertThatThrownBy {
                service.resetPassword(ResetPasswordCommand(email.value, codigo, nuevaPasswordStr))
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("inválido")
        }

        @Test
        fun `debe lanzar ValidationException si el token ya fue usado`() {
            val token = buildStoredToken(usado = true)
            every { passwordResetTokenRepository.findByHash(any()) } returns token

            assertThatThrownBy {
                service.resetPassword(ResetPasswordCommand(email.value, codigo, nuevaPasswordStr))
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("utilizado")
        }

        @Test
        fun `debe lanzar ValidationException si el token expiro`() {
            val token = buildStoredToken(expirado = true)
            every { passwordResetTokenRepository.findByHash(any()) } returns token

            assertThatThrownBy {
                service.resetPassword(ResetPasswordCommand(email.value, codigo, nuevaPasswordStr))
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("expirado")
        }

        @Test
        fun `debe lanzar ValidationException si el usuario no existe`() {
            val token = buildStoredToken()
            every { passwordResetTokenRepository.findByHash(any()) } returns token
            every { userRepository.findById(userId) } returns null

            assertThatThrownBy {
                service.resetPassword(ResetPasswordCommand(email.value, codigo, nuevaPasswordStr))
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("Usuario no encontrado")
        }

        @Test
        fun `debe lanzar ValidationException si el correo no coincide con el del token`() {
            val token = buildStoredToken()
            val otroUser = buildUser().copy(correo = Email("otro@ejemplo.com"))

            every { passwordResetTokenRepository.findByHash(any()) } returns token
            every { userRepository.findById(userId) } returns otroUser

            assertThatThrownBy {
                service.resetPassword(ResetPasswordCommand(email.value, codigo, nuevaPasswordStr))
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("inválido")
        }
    }
}