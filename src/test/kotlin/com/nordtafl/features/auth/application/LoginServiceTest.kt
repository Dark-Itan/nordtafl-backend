package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.Password
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.LoginCommand
import com.nordtafl.features.auth.domain.port.out.GeneratedTokens
import com.nordtafl.features.auth.domain.port.out.PasswordHasherPort
import com.nordtafl.features.auth.domain.port.out.TokenGeneratorPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ForbiddenException
import com.nordtafl.shared.exception.UnauthorizedException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("LoginService")
class LoginServiceTest {

    private lateinit var userRepository: UserRepositoryPort
    private lateinit var passwordHasher: PasswordHasherPort
    private lateinit var tokenGenerator: TokenGeneratorPort
    private lateinit var service: LoginService

    private val userId = UUID.randomUUID()
    private val email = Email("mariana@ejemplo.com")
    private val password = Password("PasswordSegura123!")

    private fun buildUser(
        estado: UserStatus = UserStatus.ACTIVO
    ): User = User(
        id = userId,
        nombre = "Mariana",
        correo = email,
        passwordHash = "hashed_password",
        rol = UserRole.JUGADOR,
        estado = estado,
        emailVerificado = true
    )

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        passwordHasher = mockk(relaxed = true)
        tokenGenerator = mockk(relaxed = true)
        service = LoginService(userRepository, passwordHasher, tokenGenerator)
    }

    @Nested
    @DisplayName("Casos exitosos")
    inner class SuccessCases {

        @Test
        fun `debe loguear usuario exitosamente`() {
            val user = buildUser()
            every { userRepository.findByEmail(email) } returns user
            every { passwordHasher.verify(password, user.passwordHash) } returns true
            every { tokenGenerator.generateTokens(user) } returns GeneratedTokens(
                accessToken = "access_token",
                refreshToken = "refresh_token",
                accessTokenExpiresIn = 900,
                refreshTokenExpiresIn = 604800
            )

            val result = service.login(LoginCommand(correo = email.value, password = password.value))

            assertThat(result.user.id).isEqualTo(userId)
            assertThat(result.accessToken).isEqualTo("access_token")
            assertThat(result.refreshToken).isEqualTo("refresh_token")
            assertThat(result.accessTokenExpiresIn).isEqualTo(900)
        }
    }

    @Nested
    @DisplayName("Casos de error - credenciales")
    inner class InvalidCredentials {

        @Test
        fun `debe lanzar UnauthorizedException si el correo no existe`() {
            every { userRepository.findByEmail(email) } returns null

            assertThatThrownBy {
                service.login(LoginCommand(correo = email.value, password = password.value))
            }.isInstanceOf(UnauthorizedException::class.java)
                .hasMessageContaining("Credenciales inválidas")
        }

        @Test
        fun `debe lanzar UnauthorizedException si el password es incorrecto`() {
            val user = buildUser()
            every { userRepository.findByEmail(email) } returns user
            every { passwordHasher.verify(password, user.passwordHash) } returns false

            assertThatThrownBy {
                service.login(LoginCommand(correo = email.value, password = password.value))
            }.isInstanceOf(UnauthorizedException::class.java)
                .hasMessageContaining("Credenciales inválidas")
        }
    }

    @Nested
    @DisplayName("Casos de error - estado de cuenta")
    inner class AccountStates {

        @Test
        fun `debe lanzar ForbiddenException si la cuenta está bloqueada`() {
            val user = buildUser(estado = UserStatus.BLOQUEADO)
            every { userRepository.findByEmail(email) } returns user
            every { passwordHasher.verify(password, user.passwordHash) } returns true

            assertThatThrownBy {
                service.login(LoginCommand(correo = email.value, password = password.value))
            }.isInstanceOf(ForbiddenException::class.java)
                .hasMessageContaining("bloqueada")
        }

        @Test
        fun `debe lanzar ForbiddenException si la cuenta está eliminada`() {
            val user = buildUser(estado = UserStatus.ELIMINADO)
            every { userRepository.findByEmail(email) } returns user
            every { passwordHasher.verify(password, user.passwordHash) } returns true

            assertThatThrownBy {
                service.login(LoginCommand(correo = email.value, password = password.value))
            }.isInstanceOf(ForbiddenException::class.java)
                .hasMessageContaining("eliminada")
        }

        @Test
        fun `debe lanzar ForbiddenException si la cuenta no está verificada`() {
            val user = buildUser(estado = UserStatus.PENDIENTE_VERIFICACION)
            every { userRepository.findByEmail(email) } returns user
            every { passwordHasher.verify(password, user.passwordHash) } returns true

            assertThatThrownBy {
                service.login(LoginCommand(correo = email.value, password = password.value))
            }.isInstanceOf(ForbiddenException::class.java)
                .hasMessageContaining("no ha sido verificada")
        }
    }
}