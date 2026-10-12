package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.OtpPurpose
import com.nordtafl.features.auth.domain.port.`in`.VerifyOtpCommand
import com.nordtafl.features.auth.domain.port.`in`.VerifyOtpResult
import com.nordtafl.features.auth.domain.port.`in`.VerifyOtpUseCase
import com.nordtafl.features.auth.domain.port.out.OtpRepositoryPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.time.Instant

@Service
class VerifyOtpService(
    private val userRepository: UserRepositoryPort,
    private val otpRepository: OtpRepositoryPort
) : VerifyOtpUseCase {

    private val log = LoggerFactory.getLogger(VerifyOtpService::class.java)

    @Transactional
    override fun verify(command: VerifyOtpCommand): VerifyOtpResult {
        log.info("Verificando OTP para correo: ${command.correo}, propósito: ${command.proposito}")

        val email = Email(command.correo)
        val codigoHash = hashToken(command.codigo)

        val otp = otpRepository.findLatestByEmailAndProposito(email, command.proposito)
            ?: throw ValidationException(
                code = "OTP_INVALID",
                message = "Código inválido o expirado",
                field = "codigo"
            )

        if (otp.expiraEn.isBefore(Instant.now())) {
            log.warn("OTP expirado para correo: ${command.correo}")
            throw ValidationException(
                code = "OTP_EXPIRED",
                message = "El código ha expirado. Solicita uno nuevo.",
                field = "codigo"
            )
        }

        if (otp.usado) {
            throw ValidationException(
                code = "OTP_ALREADY_USED",
                message = "El código ya fue utilizado",
                field = "codigo"
            )
        }

        if (otp.intentos >= otp.maxIntentos) {
            throw ValidationException(
                code = "OTP_MAX_ATTEMPTS",
                message = "Has superado el número máximo de intentos",
                field = "codigo"
            )
        }

        if (otp.codigoHash != codigoHash) {
            otpRepository.incrementAttempts(otp.id)
            log.warn("Intento fallido de OTP para correo: ${command.correo}")
            throw ValidationException(
                code = "OTP_INVALID",
                message = "Código inválido",
                field = "codigo"
            )
        }

        otpRepository.markAsUsed(otp.id)

        if (command.proposito == OtpPurpose.VERIFICACION_CUENTA) {
            val user = userRepository.findByEmail(email)
            if (user != null && user.estado == UserStatus.PENDIENTE_VERIFICACION) {
                val updated = user.copy(
                    estado = UserStatus.ACTIVO,
                    emailVerificado = true
                )
                userRepository.update(updated)
                log.info("Cuenta activada para usuario: ${user.id}")
            }
        }

        log.info("OTP verificado exitosamente para: ${command.correo}")

        return VerifyOtpResult(
            verificado = true,
            proposito = command.proposito,
            mensaje = "Código verificado correctamente."
        )
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}