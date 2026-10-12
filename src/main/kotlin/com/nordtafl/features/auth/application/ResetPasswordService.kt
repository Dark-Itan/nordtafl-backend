package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.Password
import com.nordtafl.features.auth.domain.port.`in`.ResetPasswordCommand
import com.nordtafl.features.auth.domain.port.`in`.ResetPasswordResult
import com.nordtafl.features.auth.domain.port.`in`.ResetPasswordUseCase
import com.nordtafl.features.auth.domain.port.out.PasswordHasherPort
import com.nordtafl.features.auth.domain.port.out.PasswordResetTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.RefreshTokenRepositoryPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant

@Service
class ResetPasswordService(
    private val userRepository: UserRepositoryPort,
    private val passwordHasher: PasswordHasherPort,
    private val passwordResetTokenRepository: PasswordResetTokenRepositoryPort,
    private val refreshTokenRepository: RefreshTokenRepositoryPort
) : ResetPasswordUseCase {

    private val log = LoggerFactory.getLogger(ResetPasswordService::class.java)

    @Transactional
    override fun resetPassword(command: ResetPasswordCommand): ResetPasswordResult {
        log.info("Restableciendo contraseña para correo: ${command.correo}")

        val email = Email(command.correo)
        val tokenHash = hashToken(command.codigo)

        val storedToken = passwordResetTokenRepository.findByHash(tokenHash)
            ?: throw ValidationException(
                code = "RESET_CODE_INVALID",
                message = "El código de recuperación es inválido",
                field = "codigo"
            )

        if (storedToken.usado) {
            throw ValidationException(
                code = "RESET_CODE_ALREADY_USED",
                message = "El código ya fue utilizado",
                field = "codigo"
            )
        }

        if (storedToken.expiresAt.isBefore(Instant.now())) {
            throw ValidationException(
                code = "RESET_CODE_EXPIRED",
                message = "El código ha expirado. Solicita uno nuevo.",
                field = "codigo"
            )
        }

        val user = userRepository.findById(storedToken.userId)
            ?: throw ValidationException(
                code = "USER_NOT_FOUND",
                message = "Usuario no encontrado",
                field = "correo"
            )

        if (user.correo.value != email.value) {
            throw ValidationException(
                code = "RESET_CODE_INVALID",
                message = "El código de recuperación es inválido",
                field = "codigo"
            )
        }

        val newPassword = Password(command.nuevaPassword)
        val newPasswordHash = passwordHasher.hash(newPassword)

        val updatedUser = user.copy(passwordHash = newPasswordHash)
        userRepository.update(updatedUser)

        passwordResetTokenRepository.markAsUsed(storedToken.id)

        refreshTokenRepository.revokeAllByUserId(user.id!!, "password_reset")

        log.info("Contraseña restablecida exitosamente para usuario: ${user.id}")

        return ResetPasswordResult(
            mensaje = "Contraseña restablecida correctamente."
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}