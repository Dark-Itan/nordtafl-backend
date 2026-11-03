package com.nordtafl.features.auth.infrastructure.adapter.`in`.rest

import com.nordtafl.features.auth.infrastructure.adapter.out.persistence.UserJpaRepository
import com.nordtafl.features.auth.infrastructure.adapter.out.persistence.UserJpaEntity
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.util.UUID

@SpringBootTest
@AutoConfigureMockMvc // Filtros activos
@ActiveProfiles("test")
@DisplayName("UserController - Tests E2E")
class UserControllerE2ETest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var userJpaRepository: UserJpaRepository

    private lateinit var testUserId: UUID

    @BeforeEach
    fun setUp() {
        // Creamos un usuario real en la base de datos para que el test de 200 funcione.
        // Asignamos el ID manualmente para evitar el error de generación de UUID.
        val user = UserJpaEntity(
            id = UUID.randomUUID(),  // <-- ESTO ES LO QUE FALTA
            nombre = "Test User",
            correo = "test_${System.currentTimeMillis()}@ejemplo.com",
            passwordHash = "hash_de_prueba",
            rol = "jugador",
            estado = "activo",
            emailVerificado = true
        )
        val savedUser = userJpaRepository.save(user)
        testUserId = savedUser.id!!
    }

    @Test
    fun `debe rechazar request sin token (401)`() {
        mockMvc.get("/api/v1/users/me")
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `debe rechazar token invalido (401)`() {
        mockMvc.get("/api/v1/users/me") {
            header("Authorization", "Bearer token_invalido")
        }
            .andExpect {
                status { isUnauthorized() }
            }
    }

    @Test
    fun `debe devolver perfil con token valido (200)`() {
        mockMvc.get("/api/v1/users/me") {
            // Inyectamos el atributo "userId" en el request,
            // simulando lo que haría el JwtAuthenticationFilter real.
            requestAttr("userId", testUserId)
        }
            .andExpect {
                status { isOk() }
            }
    }
}