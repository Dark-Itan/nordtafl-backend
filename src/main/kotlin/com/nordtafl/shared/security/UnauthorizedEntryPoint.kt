package com.nordtafl.shared.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.nordtafl.shared.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class UnauthorizedEntryPoint(
    private val objectMapper: ObjectMapper
) : AuthenticationEntryPoint {

    override fun commence(
        request: HttpServletRequest,
        response: HttpServletResponse,
        authException: AuthenticationException
    ) {
        val errorResponse = ErrorResponse(
            type = "https://api.nordtafl.app/errors/unauthorized",
            title = "No autorizado",
            status = 401,
            detail = "Se requiere autenticación para acceder a este recurso.",
            instance = request.requestURI,
            code = "UNAUTHORIZED",
            timestamp = Instant.now(),
            correlationId = UUID.randomUUID().toString()
        )

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        response.characterEncoding = "UTF-8"
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}