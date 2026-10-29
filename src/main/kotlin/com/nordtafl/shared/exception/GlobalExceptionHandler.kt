package com.nordtafl.shared.exception

import com.nordtafl.shared.config.AppProperties
import com.nordtafl.shared.dto.ErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import java.time.Instant
import java.util.UUID
import org.springframework.web.bind.MethodArgumentNotValidException

@RestControllerAdvice
class GlobalExceptionHandler(
    private val appProperties: AppProperties
) {

    private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(
        ex: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Recurso no encontrado: ${ex.message}")
        return buildResponse(
            status = HttpStatus.NOT_FOUND,
            code = ex.code,
            title = "Recurso no encontrado",
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(ConflictException::class)
    fun handleConflict(
        ex: ConflictException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Conflicto: ${ex.message}")
        return buildResponse(
            status = HttpStatus.CONFLICT,
            code = ex.code,
            title = "Conflicto",
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidation(
        ex: ValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Validación fallida: ${ex.message}")
        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            code = ex.code,
            title = "Parámetros de entrada no válidos",
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(
        ex: UnauthorizedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("No autorizado: ${ex.message}")
        return buildResponse(
            status = HttpStatus.UNAUTHORIZED,
            code = ex.code,
            title = "No autorizado",
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbidden(
        ex: ForbiddenException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Prohibido: ${ex.message}")
        return buildResponse(
            status = HttpStatus.FORBIDDEN,
            code = ex.code,
            title = "Prohibido",
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(DomainException::class)
    fun handleDomain(
        ex: DomainException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("Error de dominio: ${ex.message}")
        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            code = ex.code,
            title = "Error de dominio",
            detail = ex.message,
            request = request
        )
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val fieldError = ex.bindingResult.fieldErrors.firstOrNull()

        val detail = fieldError?.defaultMessage ?: "Parametros de entrada no validos"
        val code = when (fieldError?.field) {
            "correo" -> "INVALID_EMAIL"
            "password" -> "INVALID_PASSWORD_FORMAT"
            "nombre" -> "INVALID_NAME"
            else -> "VALIDATION_ERROR"
        }

        log.warn("Error de validacion en DTO: $detail")

        return buildResponse(
            status = HttpStatus.BAD_REQUEST,
            code = code,
            title = "Parametros de entrada no validos",
            detail = detail,
            request = request
        )
    }

    @ExceptionHandler(Exception::class)
    fun handleGeneric(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.error("Error no controlado en ${request.requestURI}", ex)
        return buildResponse(
            status = HttpStatus.INTERNAL_SERVER_ERROR,
            code = "INTERNAL_ERROR",
            title = "Error interno",
            detail = "Ocurrio un error inesperado. Intenta de nuevo mas tarde.",
            request = request
        )
    }

    private fun buildResponse(
        status: HttpStatus,
        code: String,
        title: String,
        detail: String,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            type = "https://api.nordtafl.app/errors/${code.lowercase().replace("_", "-")}",
            title = title,
            status = status.value(),
            detail = detail,
            instance = request.requestURI,
            code = code,
            timestamp = Instant.now(),
            correlationId = UUID.randomUUID().toString()
        )
        return ResponseEntity.status(status).body(errorResponse)
    }
}