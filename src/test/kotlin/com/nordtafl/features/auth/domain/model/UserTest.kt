package com.nordtafl.features.auth.domain.model

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.shared.exception.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.UUID

@DisplayName("User Entity")
class UserTest {

    private fun buildUser(
        nombre: String = "Mariana López",
        email: String = "mariana@ejemplo.com",
        rol: UserRole = UserRole.JUGADOR,
        estado: UserStatus = UserStatus.ACTIVO,
        emailVerificado: Boolean = false,
        googleId: String? = null,
        avatarUrl: String? = null
    ): User {
        return User(
            id = UUID.randomUUID(),
            nombre = nombre,
            correo = Email(email),
            passwordHash = "\$2a\$12\$hash_de_prueba",
            rol = rol,
            estado = estado,
            emailVerificado = emailVerificado,
            googleId = googleId,
            avatarUrl = avatarUrl
        )
    }

    @Nested
    @DisplayName("Validación del nombre")
    inner class NameValidation {

        @Test
        fun `debe aceptar nombre valido`() {
            val user = buildUser(nombre = "Mariana López")
            assertThat(user.nombre).isEqualTo("Mariana López")
        }

        @Test
        fun `debe rechazar nombre vacio`() {
            assertThatThrownBy { buildUser(nombre = "") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("vacío")
        }

        @Test
        fun `debe rechazar nombre en blanco`() {
            assertThatThrownBy { buildUser(nombre = "   ") }
                .isInstanceOf(ValidationException::class.java)
        }

        @Test
        fun `debe rechazar nombre de 1 caracter`() {
            assertThatThrownBy { buildUser(nombre = "A") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("2 y 100")
        }

        @Test
        fun `debe rechazar nombre mayor a 100 caracteres`() {
            val longName = "A".repeat(101)
            assertThatThrownBy { buildUser(nombre = longName) }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("100")
        }
    }

    @Nested
    @DisplayName("Métodos de negocio")
    inner class BusinessMethods {

        @Test
        fun `isActive debe retornar true cuando el estado es ACTIVO`() {
            val user = buildUser(estado = UserStatus.ACTIVO)
            assertThat(user.isActive()).isTrue()
        }

        @Test
        fun `isActive debe retornar false cuando el estado es BLOQUEADO`() {
            val user = buildUser(estado = UserStatus.BLOQUEADO)
            assertThat(user.isActive()).isFalse()
        }

        @Test
        fun `isVerified debe retornar true cuando emailVerificado es true`() {
            val user = buildUser(emailVerificado = true)
            assertThat(user.isVerified()).isTrue()
        }

        @Test
        fun `isVerified debe retornar false cuando emailVerificado es false`() {
            val user = buildUser(emailVerificado = false)
            assertThat(user.isVerified()).isFalse()
        }

        @Test
        fun `isGoogleAccount debe retornar true cuando googleId no es null`() {
            val user = buildUser(googleId = "1234567890")
            assertThat(user.isGoogleAccount()).isTrue()
        }

        @Test
        fun `isGoogleAccount debe retornar false cuando googleId es null`() {
            val user = buildUser(googleId = null)
            assertThat(user.isGoogleAccount()).isFalse()
        }
    }

    @Nested
    @DisplayName("Roles y estados")
    inner class RolesAndStatus {

        @Test
        fun `debe aceptar rol JUGADOR`() {
            val user = buildUser(rol = UserRole.JUGADOR)
            assertThat(user.rol).isEqualTo(UserRole.JUGADOR)
        }

        @Test
        fun `debe aceptar rol ADMIN`() {
            val user = buildUser(rol = UserRole.ADMIN)
            assertThat(user.rol).isEqualTo(UserRole.ADMIN)
        }

        @Test
        fun `debe aceptar estado PENDIENTE_VERIFICACION`() {
            val user = buildUser(estado = UserStatus.PENDIENTE_VERIFICACION)
            assertThat(user.estado).isEqualTo(UserStatus.PENDIENTE_VERIFICACION)
        }
    }
}