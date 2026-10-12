package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.`in`.RequestOtpCommand
import com.nordtafl.features.auth.domain.port.`in`.RequestOtpResult
import com.nordtafl.features.auth.domain.port.`in`.RequestOtpUseCase
import com.nordtafl.features.auth.domain.port.out.OtpRepositoryPort
import com.nordtafl.features.auth.domain.port.out.OtpSenderPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Duration
import java.time.Instant

@Service
class RequestOtpService(
    private val userRepository: UserRepositoryPort,
    private val otpRepository: OtpRepositoryPort,
    private val otpSender: OtpSenderPort
) : RequestOtpUseCase {

    private val log = LoggerFactory.getLogger(RequestOtpService::class.java)
    private val secureRandom = SecureRandom()

    @Transactional
    override fun request(command: RequestOtpCommand): RequestOtpResult {
        log.info("Solicitando OTP para correo: ${command.correo}, propósito: ${command.proposito}")

        val email = Email(command.correo)
        val user = userRepository.findByEmail(email)

        if (user == null) {
            log.warn("Solicitud de OTP para correo no registrado: ${command.correo}")
            return RequestOtpResult(
                mensaje = "Si el correo existe, recibirás un código en breve.",
                expiraEn = 300
            )
        }

        otpRepository.invalidatePrevious(email, command.proposito)

        val codigo = generateOtpCode()
        val codigoHash = hashToken(codigo)
        val expiraEn = Instant.now().plus(Duration.ofMinutes(5))

        otpRepository.save(
            userId = user.id,
            correo = email,
            codigoHash = codigoHash,
            proposito = command.proposito,
            expiraEn = expiraEn
        )

        otpSender.sendOtp(email = email, codigo = codigo, proposito = command.proposito)

        log.info("OTP enviado exitosamente a: ${command.correo}")

        return RequestOtpResult(
            mensaje = "Si el correo existe, recibirás un código en breve.",
            expiraEn = 300
        )
    }

    private fun generateOtpCode(): String {
        return (100000 + secureRandom.nextInt(900000)).toString()
    }

    private fun hashToken(token: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(token.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}