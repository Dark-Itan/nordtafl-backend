package com.nordtafl.features.auth.domain.model.value

import com.nordtafl.shared.exception.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Password Value Object")
class PasswordTest {

    @Nested
    @DisplayName("Casos válidos")
    inner class ValidPasswords {

        @Test
        fun `debe aceptar password con todos los requisitos`() {
            val password = Password("PasswordSegura123!")
            assertThat(password.value).isEqualTo("PasswordSegura123!")
        }

        @Test
        fun `debe aceptar password de 12 caracteres exactos`() {
            val password = Password("Password123!")
            assertThat(password.value.length).isEqualTo(12)
        }

        @Test
        fun `debe aceptar password con multiples caracteres especiales`() {
            val password = Password("P@ssw0rd!#$%")
            assertThat(password.value).isEqualTo("P@ssw0rd!#$%")
        }
    }

    @Nested
    @DisplayName("Casos inválidos")
    inner class InvalidPasswords {

        @Test
        fun `debe rechazar password vacio`() {
            assertThatThrownBy { Password("") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("12 caracteres")
        }

        @Test
        fun `debe rechazar password menor a 12 caracteres`() {
            assertThatThrownBy { Password("Password1!") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("12 caracteres")
        }

        @Test
        fun `debe rechazar password mayor a 128 caracteres`() {
            val longPassword = "P" + "a".repeat(128) + "1!"
            assertThatThrownBy { Password(longPassword) }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("128")
        }

        @Test
        fun `debe rechazar password sin mayuscula`() {
            assertThatThrownBy { Password("password123!") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("mayúscula")
        }

        @Test
        fun `debe rechazar password sin numero`() {
            assertThatThrownBy { Password("PasswordSegura!") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("número")
        }

        @Test
        fun `debe rechazar password sin caracter especial`() {
            assertThatThrownBy { Password("PasswordSegura123") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("carácter especial")
        }
    }
}