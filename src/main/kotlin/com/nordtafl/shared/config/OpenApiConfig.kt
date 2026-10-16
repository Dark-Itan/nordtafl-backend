package com.nordtafl.shared.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.servers.Server
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Ingresa el token JWT con el prefijo 'Bearer '"
)
class OpenApiConfig(
    private val appProperties: AppProperties
) {

    @Bean
    fun customOpenAPI(): OpenAPI {
        return OpenAPI()
            .info(
                Info()
                    .title("NordTafl API")
                    .description("API RESTful del juego de Hnefatafl multijugador por turnos")
                    .version(appProperties.version)
                    .contact(
                        Contact()
                            .name("NordTafl Team")
                            .email("team@nordtafl.app")
                    )
                    .license(
                        License()
                            .name("MIT")
                            .url("https://opensource.org/licenses/MIT")
                    )
            )
            .addSecurityItem(SecurityRequirement().addList("bearerAuth"))
            .components(
                Components()
                    .addSecuritySchemes(
                        "bearerAuth",
                        io.swagger.v3.oas.models.security.SecurityScheme()
                            .type(io.swagger.v3.oas.models.security.SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .servers(
                listOf(
                    Server().url("http://localhost:8080").description("Desarrollo"),
                    Server().url("https://<dominio-railway>").description("Producción (pendiente)")
                )
            )
    }
}