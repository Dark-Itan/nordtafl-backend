package com.nordtafl.features.auth.domain.model.value

import com.nordtafl.shared.exception.ValidationException
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@DisplayName("Email Value Object")
class EmailTest {

    @Nested
    @DisplayName("Casos válidos")
    inner class ValidEmails {

        @Test
        fun `debe aceptar un correo valido`() {
            val email = Email("mariana@ejemplo.com")
            assertThat(email.value).isEqualTo("mariana@ejemplo.com")
        }

        @Test
        fun `debe normalizar el correo a minusculas`() {
            val email = Email("MARIANA@EJEMPLO.COM")
            assertThat(email.normalized()).isEqualTo("mariana@ejemplo.com")
        }

        @Test
        fun `debe eliminar espacios en los extremos`() {
            val email = Email("  mariana@ejemplo.com  ")
            assertThat(email.normalized()).isEqualTo("mariana@ejemplo.com")
        }

        @Test
        fun `debe aceptar correos con subdominios`() {
            val email = Email("user@mail.ejemplo.com")
            assertThat(email.value).isEqualTo("user@mail.ejemplo.com")
        }

        @Test
        fun `debe aceptar correos con plus`() {
            val email = Email("user+tag@ejemplo.com")
            assertThat(email.value).isEqualTo("user+tag@ejemplo.com")
        }
    }

    @Nested
    @DisplayName("Casos inválidos")
    inner class InvalidEmails {

        @Test
        fun `debe rechazar correo vacio`() {
            assertThatThrownBy { Email("") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("vacío")
        }

        @Test
        fun `debe rechazar correo en blanco`() {
            assertThatThrownBy { Email("   ") }
                .isInstanceOf(ValidationException::class.java)
        }

        @Test
        fun `debe rechazar correo sin arroba`() {
            assertThatThrownBy { Email("mariana.ejemplo.com") }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("formato válido")
        }

        @Test
        fun `debe rechazar correo sin dominio`() {
            assertThatThrownBy { Email("mariana@") }
                .isInstanceOf(ValidationException::class.java)
        }

        @Test
        fun `debe rechazar correo sin extension`() {
            assertThatThrownBy { Email("mariana@ejemplo") }
                .isInstanceOf(ValidationException::class.java)
        }

        @Test
        fun `debe rechazar correo con mas de 254 caracteres`() {
            val longEmail = "a".repeat(250) + "@ejemplo.com"
            assertThatThrownBy { Email(longEmail) }
                .isInstanceOf(ValidationException::class.java)
                .hasMessageContaining("254")
        }

        @Test
        fun `debe rechazar correo con espacios internos`() {
            assertThatThrownBy { Email("mariana lopez@ejemplo.com") }
                .isInstanceOf(ValidationException::class.java)
        }
    }
}