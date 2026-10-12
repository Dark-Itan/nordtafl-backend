package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.`in`.ForgotPasswordCommand
import com.nordtafl.features.auth.domain.port.`in`.ForgotPasswordResult
import com.nordtafl.features.auth.domain.port.`in`.ForgotPasswordUseCase
import com.nordtafl.features.auth.domain.port.out.PasswordResetSenderPort
import com.nordtafl.features.auth.domain.port.out.PasswordResetTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant
import java.util.Base64

@Service
class ForgotPasswordService(
    private val userRepository: UserRepositoryPort,
    private val passwordResetTokenRepository: PasswordResetTokenRepositoryPort,
    private val passwordResetSender: PasswordResetSenderPort
) : ForgotPasswordUseCase {

    private val log = LoggerFactory.getLogger(ForgotPasswordService::class.java)
    private val secureRandom = SecureRandom()

    @Transactional
    override fun forgotPassword(command: ForgotPasswordCommand): ForgotPasswordResult {
        log.info("Solicitando recuperación de contraseña para correo: ${command.correo}")

        val email = Email(command.correo)
        val user = userRepository.findByEmail(email)

        if (user == null) {
            log.warn("Solicitud de recuperación para correo no registrado: ${command.correo}")
            return ForgotPasswordResult(
                mensaje = "Si el correo existe, recibirás un código de recuperación.",
                expiraEn = 600
            )
        }

        passwordResetTokenRepository.invalidatePrevious(user.id!!)

        val token = generateToken()
        val tokenHash = hashToken(token)
        val expiresAt = Instant.now().plus(Duration.ofMinutes(10))

        passwordResetTokenRepository.save(
            userId = user.id!!,
            tokenHash = tokenHash,
            expiresAt = expiresAt
        )

        passwordResetSender.sendPasswordResetToken(email = email, token = token)

        log.info("Token de recuperación enviado a: ${command.correo}")

        return ForgotPasswordResult(
            mensaje = "Si el correo existe, recibirás un código de recuperación.",
            expiraEn = 600
        )
    }

    private fun generateToken(): String {
        val bytes = ByteArray(32)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}