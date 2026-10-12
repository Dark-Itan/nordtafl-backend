package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.GoogleLoginCommand
import com.nordtafl.features.auth.domain.port.`in`.GoogleLoginResult
import com.nordtafl.features.auth.domain.port.`in`.GoogleLoginUseCase
import com.nordtafl.features.auth.domain.port.out.GoogleTokenValidatorPort
import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.StoredRefreshToken
import com.nordtafl.features.auth.domain.port.out.TokenGeneratorPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant
import java.util.UUID

@Service
class GoogleLoginService(
    private val userRepository: UserRepositoryPort,
    private val googleTokenValidator: GoogleTokenValidatorPort,
    private val tokenGenerator: TokenGeneratorPort,
    private val refreshTokenRepository: RefreshTokenRepositoryPort
) : GoogleLoginUseCase {

    private val log = LoggerFactory.getLogger(GoogleLoginService::class.java)

    @Transactional
    override fun loginWithGoogle(command: GoogleLoginCommand): GoogleLoginResult {
        log.info("Intento de login con Google")

        val googleUser = googleTokenValidator.validateIdToken(command.idToken)

        if (!googleUser.emailVerified) {
            log.warn("Correo de Google no verificado: ${googleUser.email}")
            throw com.nordtafl.shared.exception.UnauthorizedException(
                code = "GOOGLE_EMAIL_NOT_VERIFIED",
                message = "El correo de Google no está verificado"
            )
        }

        val email = Email(googleUser.email)
        val existingUser = userRepository.findByEmail(email)

        val (user, isNewUser) = when {
            existingUser == null -> {
                log.info("Creando usuario nuevo con Google: ${googleUser.email}")
                val newUser = User(
                    id = null,
                    nombre = googleUser.name,
                    correo = email,
                    passwordHash = "",
                    rol = UserRole.JUGADOR,
                    estado = UserStatus.ACTIVO,
                    emailVerificado = true,
                    avatarUrl = googleUser.picture,
                    googleId = googleUser.sub
                )
                Pair(userRepository.save(newUser), true)
            }

            existingUser.googleId == null -> {
                log.info("Vinculando Google a usuario existente: ${googleUser.email}")
                val updated = existingUser.copy(
                    googleId = googleUser.sub,
                    avatarUrl = existingUser.avatarUrl ?: googleUser.picture,
                    emailVerificado = true,
                    estado = UserStatus.ACTIVO
                )
                Pair(userRepository.update(updated), false)
            }

            else -> {
                log.info("Login con Google para usuario existente: ${googleUser.email}")
                Pair(existingUser, false)
            }
        }

        val tokens = tokenGenerator.generateTokens(user)

        val refreshTokenHash = hashToken(tokens.refreshToken)
        refreshTokenRepository.save(
            StoredRefreshToken(
                id = UUID.randomUUID(),
                userId = user.id!!,
                tokenHash = refreshTokenHash,
                revoked = false,
                expiresAt = Instant.now().plusSeconds(tokens.refreshTokenExpiresIn)
            )
        )

        log.info("Login con Google exitoso para usuario: ${user.id}")

        return GoogleLoginResult(
            user = user,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            accessTokenExpiresIn = tokens.accessTokenExpiresIn,
            isNewUser = isNewUser
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}