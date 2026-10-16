package com.nordtafl.features.auth.infrastructure.adapter.`in`.rest

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.port.`in`.ForgotPasswordCommand
import com.nordtafl.features.auth.domain.port.`in`.ForgotPasswordUseCase
import com.nordtafl.features.auth.domain.port.`in`.GoogleLoginCommand
import com.nordtafl.features.auth.domain.port.`in`.GoogleLoginUseCase
import com.nordtafl.features.auth.domain.port.`in`.LoginCommand
import com.nordtafl.features.auth.domain.port.`in`.LoginUseCase
import com.nordtafl.features.auth.domain.port.`in`.LogoutCommand
import com.nordtafl.features.auth.domain.port.`in`.LogoutUseCase
import com.nordtafl.features.auth.domain.port.`in`.OtpPurpose
import com.nordtafl.features.auth.domain.port.`in`.RefreshTokenCommand
import com.nordtafl.features.auth.domain.port.`in`.RefreshTokenUseCase
import com.nordtafl.features.auth.domain.port.`in`.RegisterUserCommand
import com.nordtafl.features.auth.domain.port.`in`.RegisterUserUseCase
import com.nordtafl.features.auth.domain.port.`in`.RequestOtpCommand
import com.nordtafl.features.auth.domain.port.`in`.RequestOtpUseCase
import com.nordtafl.features.auth.domain.port.`in`.ResetPasswordCommand
import com.nordtafl.features.auth.domain.port.`in`.ResetPasswordUseCase
import com.nordtafl.features.auth.domain.port.`in`.VerifyOtpCommand
import com.nordtafl.features.auth.domain.port.`in`.VerifyOtpUseCase
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterChallengeCommand
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterChallengeResult
import com.nordtafl.features.auth.domain.port.`in`.WebAuthnRegisterChallengeUseCase
import com.nordtafl.features.auth.infrastructure.adapter.`in`.rest.dto.*
import com.nordtafl.shared.config.MetaFactory
import com.nordtafl.shared.dto.ApiResponse
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(
    private val registerUserUseCase: RegisterUserUseCase,
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val requestOtpUseCase: RequestOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val refreshTokenUseCase: RefreshTokenUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val resetPasswordUseCase: ResetPasswordUseCase,
    private val googleLoginUseCase: GoogleLoginUseCase,
    private val webAuthnRegisterChallengeUseCase: WebAuthnRegisterChallengeUseCase,
    private val metaFactory: MetaFactory
) {

    private val log = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    fun register(@Valid @RequestBody request: RegisterRequest): ResponseEntity<ApiResponse<UserResponse>> {
        log.info("POST /api/v1/auth/register")

        val user = registerUserUseCase.register(
            RegisterUserCommand(
                nombre = request.nombre,
                correo = request.correo,
                password = request.password,
                rol = request.rol
            )
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ApiResponse.success(user.toResponse(), metaFactory.build()))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody request: LoginRequest): ResponseEntity<ApiResponse<LoginResponse>> {
        log.info("POST /api/v1/auth/login")

        val result = loginUseCase.login(
            LoginCommand(correo = request.correo, password = request.password)
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                LoginResponse(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    expiresIn = result.accessTokenExpiresIn,
                    tokenType = "Bearer",
                    user = result.user.toResponse()
                ),
                metaFactory.build()
            )
        )
    }

    @PostMapping("/logout")
    fun logout(@Valid @RequestBody request: LogoutRequest): ResponseEntity<Void> {
        log.info("POST /api/v1/auth/logout")

        logoutUseCase.logout(
            LogoutCommand(
                accessToken = "",
                refreshToken = request.refreshToken
            )
        )

        return ResponseEntity.noContent().build()
    }

    @PostMapping("/otp/request")
    fun requestOtp(@Valid @RequestBody request: RequestOtpRequest): ResponseEntity<ApiResponse<RequestOtpResponse>> {
        log.info("POST /api/v1/auth/otp/request")

        val result = requestOtpUseCase.request(
            RequestOtpCommand(
                correo = request.correo,
                proposito = parseOtpPurpose(request.proposito)
            )
        )

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(
                RequestOtpResponse(mensaje = result.mensaje, expiraEn = result.expiraEn),
                metaFactory.build()
            ))
    }

    @PostMapping("/otp/verify")
    fun verifyOtp(@Valid @RequestBody request: VerifyOtpRequest): ResponseEntity<ApiResponse<VerifyOtpResponse>> {
        log.info("POST /api/v1/auth/otp/verify")

        val result = verifyOtpUseCase.verify(
            VerifyOtpCommand(
                correo = request.correo,
                codigo = request.codigo,
                proposito = parseOtpPurpose(request.proposito)
            )
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                VerifyOtpResponse(
                    verificado = result.verificado,
                    proposito = result.proposito.name,
                    mensaje = result.mensaje
                ),
                metaFactory.build()
            )
        )
    }

    @PostMapping("/token/refresh")
    fun refreshToken(@Valid @RequestBody request: RefreshTokenRequest): ResponseEntity<ApiResponse<RefreshTokenResponse>> {
        log.info("POST /api/v1/auth/token/refresh")

        val result = refreshTokenUseCase.refresh(
            RefreshTokenCommand(refreshToken = request.refreshToken)
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                RefreshTokenResponse(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    expiresIn = result.accessTokenExpiresIn,
                    tokenType = "Bearer"
                ),
                metaFactory.build()
            )
        )
    }

    @PostMapping("/password/forgot")
    fun forgotPassword(@Valid @RequestBody request: ForgotPasswordRequest): ResponseEntity<ApiResponse<ForgotPasswordResponse>> {
        log.info("POST /api/v1/auth/password/forgot")

        val result = forgotPasswordUseCase.forgotPassword(
            ForgotPasswordCommand(correo = request.correo)
        )

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success(
                ForgotPasswordResponse(mensaje = result.mensaje, expiraEn = result.expiraEn),
                metaFactory.build()
            ))
    }

    @PostMapping("/password/reset")
    fun resetPassword(@Valid @RequestBody request: ResetPasswordRequest): ResponseEntity<ApiResponse<ResetPasswordResponse>> {
        log.info("POST /api/v1/auth/password/reset")

        val result = resetPasswordUseCase.resetPassword(
            ResetPasswordCommand(
                correo = request.correo,
                codigo = request.codigo,
                nuevaPassword = request.nuevaPassword
            )
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                ResetPasswordResponse(mensaje = result.mensaje),
                metaFactory.build()
            )
        )
    }

    @PostMapping("/google")
    fun googleLogin(@Valid @RequestBody request: GoogleLoginRequest): ResponseEntity<ApiResponse<GoogleLoginResponse>> {
        log.info("POST /api/v1/auth/google")

        val result = googleLoginUseCase.loginWithGoogle(
            GoogleLoginCommand(idToken = request.idToken)
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                GoogleLoginResponse(
                    accessToken = result.accessToken,
                    refreshToken = result.refreshToken,
                    expiresIn = result.accessTokenExpiresIn,
                    tokenType = "Bearer",
                    user = result.user.toResponse(),
                    isNewUser = result.isNewUser
                ),
                metaFactory.build()
            )
        )
    }

    @PostMapping("/webauthn/register/challenge")
    fun webAuthnRegisterChallenge(
        @RequestBody request: Map<String, String>
    ): ResponseEntity<ApiResponse<WebAuthnRegisterChallengeResult>> {
        log.info("POST /api/v1/auth/webauthn/register/challenge")

        val userId = UUID.fromString(
            request["userId"] ?: throw IllegalArgumentException("userId es obligatorio")
        )

        val result = webAuthnRegisterChallengeUseCase.generateChallenge(
            WebAuthnRegisterChallengeCommand(userId = userId)
        )

        return ResponseEntity.ok(ApiResponse.success(result, metaFactory.build()))
    }

    private fun parseOtpPurpose(purpose: String): OtpPurpose {
        return when (purpose.uppercase()) {
            "VERIFICACION_CUENTA" -> OtpPurpose.VERIFICACION_CUENTA
            "RECUPERACION_PASSWORD" -> OtpPurpose.RECUPERACION_PASSWORD
            "ACCION_SENSIBLE" -> OtpPurpose.ACCION_SENSIBLE
            else -> throw IllegalArgumentException("Propósito inválido: $purpose")
        }
    }

    private fun User.toResponse(): UserResponse {
        return UserResponse(
            id = this.id.toString(),
            nombre = this.nombre,
            correo = this.correo.value,
            rol = this.rol.name.lowercase(),
            estado = this.estado.name.lowercase(),
            emailVerificado = this.emailVerificado,
            avatarUrl = this.avatarUrl,
            ultimoAcceso = this.ultimoAcceso,
            creadoEn = this.createdAt
        )
    }
}