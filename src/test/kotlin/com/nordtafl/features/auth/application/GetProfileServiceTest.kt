package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.GetProfileCommand
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ResourceNotFoundException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("GetProfileService")
class GetProfileServiceTest {

    private lateinit var userRepository: UserRepositoryPort
    private lateinit var service: GetProfileService

    private val userId = UUID.randomUUID()
    private val email = Email("mariana@ejemplo.com")

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        service = GetProfileService(userRepository)
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
        fun `debe devolver el perfil del usuario correctamente`() {
            val user = buildUser()
            every { userRepository.findById(userId) } returns user

            val result = service.getProfile(GetProfileCommand(userId))

            assertThat(result.id).isEqualTo(userId)
            assertThat(result.nombre).isEqualTo("Mariana")
            assertThat(result.correo.value).isEqualTo("mariana@ejemplo.com")
            assertThat(result.rol).isEqualTo(UserRole.JUGADOR)
        }
    }

    @Nested
    @DisplayName("Casos de error")
    inner class ErrorCases {

        @Test
        fun `debe lanzar ResourceNotFoundException si el usuario no existe`() {
            every { userRepository.findById(userId) } returns null

            assertThatThrownBy {
                service.getProfile(GetProfileCommand(userId))
            }.isInstanceOf(ResourceNotFoundException::class.java)
                .hasMessageContaining("Usuario")
        }
    }
}