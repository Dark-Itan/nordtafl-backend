package com.nordtafl.features.auth.infrastructure.adapter.`in`.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.nordtafl.features.auth.infrastructure.adapter.`in`.rest.dto.RegisterRequest
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // Desactiva los filtros de seguridad (JWT/CSRF)
@ActiveProfiles("test")                   // Usa la configuración H2
@DisplayName("AuthController - Tests E2E")
class AuthControllerE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Test
    fun `debe rechazar correo invalido (400)`() {
        val request = RegisterRequest(
            nombre = "Ataque",
            correo = "no-es-email",
            password = "PasswordSegura123!",
            rol = "jugador"
        )

        mockMvc.post("/api/v1/auth/register") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(request)
        }
            .andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value("INVALID_EMAIL") }
            }
    }
}