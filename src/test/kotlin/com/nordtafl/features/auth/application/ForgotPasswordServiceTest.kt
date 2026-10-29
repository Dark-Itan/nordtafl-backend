package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.ForgotPasswordCommand
import com.nordtafl.features.auth.domain.port.out.PasswordResetSenderPort
import com.nordtafl.features.auth.domain.port.out.PasswordResetTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("ForgotPasswordService")
class ForgotPasswordServiceTest {

    private lateinit var userRepository: UserRepositoryPort
    private lateinit var passwordResetTokenRepository: PasswordResetTokenRepositoryPort
    private lateinit var passwordResetSender: PasswordResetSenderPort
    private lateinit var service: ForgotPasswordService

    private val userId = UUID.randomUUID()
    private val email = Email("mariana@ejemplo.com")

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        passwordResetTokenRepository = mockk(relaxed = true)
        passwordResetSender = mockk(relaxed = true)
        service = ForgotPasswordService(
            userRepository,
            passwordResetTokenRepository,
            passwordResetSender
        )
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

    @Nested
    @DisplayName("Casos exitosos")
    inner class SuccessCases {

        @Test
        fun `debe generar token y enviar correo si el usuario existe`() {
            val user = buildUser()
            every { userRepository.findByEmail(email) } returns user

            val result = service.forgotPassword(ForgotPasswordCommand(email.value))

            assertThat(result.mensaje).contains("Si el correo existe")
            assertThat(result.expiraEn).isEqualTo(600)

            // Verifica que se invalidaron tokens previos
            verify(exactly = 1) { passwordResetTokenRepository.invalidatePrevious(userId) }

            // Verifica que se guardó el nuevo token
            verify(exactly = 1) { passwordResetTokenRepository.save(any(), any(), any()) }

            // Verifica que se envió el correo al usuario correcto
            verify(exactly = 1) { passwordResetSender.sendPasswordResetToken(email, any()) }
        }
    }

    @Nested
    @DisplayName("Seguridad y privacidad")
    inner class SecurityCases {

        @Test
        fun `debe retornar mensaje generico si el correo no existe (evita enumeracion)`() {
            every { userRepository.findByEmail(email) } returns null

            val result = service.forgotPassword(ForgotPasswordCommand(email.value))

            // El mensaje debe ser idéntico al caso exitoso para no revelar si el correo existe
            assertThat(result.mensaje).contains("Si el correo existe")

            // NO debe guardar token ni enviar correo
            verify(exactly = 0) { passwordResetTokenRepository.save(any(), any(), any()) }
            verify(exactly = 0) { passwordResetSender.sendPasswordResetToken(email, any()) }
        }
    }
}