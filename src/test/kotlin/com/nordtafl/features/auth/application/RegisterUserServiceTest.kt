package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.Password
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.RegisterUserCommand
import com.nordtafl.features.auth.domain.port.out.PasswordHasherPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ConflictException
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
import java.util.UUID

@DisplayName("RegisterUserService")
class RegisterUserServiceTest {

    private lateinit var userRepository: UserRepositoryPort
    private lateinit var passwordHasher: PasswordHasherPort
    private lateinit var service: RegisterUserService

    @BeforeEach
    fun setUp() {
        userRepository = mockk(relaxed = true)
        passwordHasher = mockk(relaxed = true)
        service = RegisterUserService(userRepository, passwordHasher)
    }

    @Nested
    @DisplayName("Casos exitosos")
    inner class SuccessCases {

        @Test
        fun `debe registrar usuario exitosamente`() {
            val email = Email("mariana@ejemplo.com")
            val password = Password("PasswordSegura123!")

            every { userRepository.existsByEmail(email) } returns false
            every { passwordHasher.hash(password) } returns "\$2a\$12\$hashed"
            every { userRepository.save(any()) } answers {
                firstArg<User>().copy(id = UUID.randomUUID())
            }

            val result = service.register(
                RegisterUserCommand(
                    nombre = "Mariana López",
                    correo = "mariana@ejemplo.com",
                    password = "PasswordSegura123!",
                    rol = "jugador"
                )
            )

            assertThat(result.nombre).isEqualTo("Mariana López")
            assertThat(result.correo.value).isEqualTo("mariana@ejemplo.com")
            assertThat(result.rol).isEqualTo(UserRole.JUGADOR)
            assertThat(result.estado).isEqualTo(UserStatus.PENDIENTE_VERIFICACION)
            verify(exactly = 1) { userRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("Casos de error - validación")
    inner class ValidationCases {

        @Test
        fun `debe rechazar correo inválido`() {
            assertThatThrownBy {
                service.register(
                    RegisterUserCommand(
                        nombre = "Mariana",
                        correo = "no-es-email",
                        password = "PasswordSegura123!",
                        rol = "jugador"
                    )
                )
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("formato válido")
        }

        @Test
        fun `debe rechazar password débil`() {
            assertThatThrownBy {
                service.register(
                    RegisterUserCommand(
                        nombre = "Mariana",
                        correo = "mariana@ejemplo.com",
                        password = "123",
                        rol = "jugador"
                    )
                )
            }.isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("12 caracteres")
        }

        @Test
        fun `debe rechazar nombre vacío`() {
            assertThatThrownBy {
                service.register(
                    RegisterUserCommand(
                        nombre = "",
                        correo = "mariana@ejemplo.com",
                        password = "PasswordSegura123!",
                        rol = "jugador"
                    )
                )
            }.isInstanceOf(ValidationException::class.java)
        }
    }

    @Nested
    @DisplayName("Casos de error - conflicto")
    inner class ConflictCases {

        @Test
        fun `debe lanzar ConflictException si el correo ya existe`() {
            val email = Email("duplicado@ejemplo.com")
            every { userRepository.existsByEmail(email) } returns true

            assertThatThrownBy {
                service.register(
                    RegisterUserCommand(
                        nombre = "Mariana",
                        correo = "duplicado@ejemplo.com",
                        password = "PasswordSegura123!",
                        rol = "jugador"
                    )
                )
            }.isInstanceOf(ConflictException::class.java)
                .hasMessageContaining("ya está registrado")

            verify(exactly = 0) { userRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("Ataques de seguridad")
    inner class SecurityCases {

        @Test
        fun `debe rechazar SQL injection en correo`() {
            assertThatThrownBy {
                service.register(
                    RegisterUserCommand(
                        nombre = "Ataque",
                        correo = "admin'--",
                        password = "PasswordSegura123!",
                        rol = "jugador"
                    )
                )
            }.isInstanceOf(ValidationException::class.java)
        }

        @Test
        fun `debe rechazar XSS en nombre`() {
            val email = Email("xss@ejemplo.com")
            val password = Password("PasswordSegura123!")

            every { userRepository.existsByEmail(email) } returns false
            every { passwordHasher.hash(password) } returns "hashed"
            every { userRepository.save(any()) } answers {
                firstArg<User>().copy(id = UUID.randomUUID())
            }

            val result = service.register(
                RegisterUserCommand(
                    nombre = "<script>alert('xss')</script>",
                    correo = "xss@ejemplo.com",
                    password = "PasswordSegura123!",
                    rol = "jugador"
                )
            )

            assertThat(result.nombre).contains("<script>")
        }

        @Test
        fun `debe aceptar comandos shell como string sin ejecutarlos`() {
            val email = Email("bash@ejemplo.com")
            val password = Password("PasswordSegura123!")

            every { userRepository.existsByEmail(email) } returns false
            every { passwordHasher.hash(password) } returns "hashed"
            every { userRepository.save(any()) } answers {
                firstArg<User>().copy(id = UUID.randomUUID())
            }

            val result = service.register(
                RegisterUserCommand(
                    nombre = "/bin/bash",
                    correo = "bash@ejemplo.com",
                    password = "PasswordSegura123!",
                    rol = "jugador"
                )
            )

            assertThat(result.nombre).isEqualTo("/bin/bash")
        }
    }
}