package com.nordtafl.features.auth.infrastructure.adapter.out.email

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.`in`.OtpPurpose
import com.nordtafl.features.auth.domain.port.out.OtpSenderPort
import com.nordtafl.features.auth.domain.port.out.PasswordResetSenderPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.stereotype.Component

@Component
class BrevoEmailAdapter(
    private val mailSender: JavaMailSender,
    @Value("\${app.brevo.from-email:noreply@nordtafl.app}")
    private val fromEmail: String
) : OtpSenderPort, PasswordResetSenderPort {

    private val log = LoggerFactory.getLogger(BrevoEmailAdapter::class.java)

    override fun sendOtp(email: Email, codigo: String, proposito: OtpPurpose) {
        log.info("Enviando OTP a: ${email.value}, proposito: $proposito")

        val subject = when (proposito) {
            OtpPurpose.VERIFICACION_CUENTA -> "Verifica tu cuenta en NordTafl"
            OtpPurpose.RECUPERACION_PASSWORD -> "Recupera tu contrasena de NordTafl"
            OtpPurpose.ACCION_SENSIBLE -> "Codigo de verificacion NordTafl"
        }

        val body = buildOtpBody(codigo)

        sendEmail(to = email.value, subject = subject, body = body)
    }

    override fun sendPasswordResetToken(email: Email, token: String) {
        log.info("Enviando token de recuperacion a: ${email.value}")

        val subject = "Recupera tu contrasena de NordTafl"
        val body = buildPasswordResetBody(token)

        sendEmail(to = email.value, subject = subject, body = body)
    }

    private fun sendEmail(to: String, subject: String, body: String) {
        try {
            val message = SimpleMailMessage().apply {
                setFrom(fromEmail)
                setTo(to)
                setSubject(subject)
                setText(body)
            }

            mailSender.send(message)
            log.debug("Correo enviado exitosamente a: $to")
        } catch (ex: Exception) {
            log.error("Error enviando correo a $to: ${ex.message}", ex)
            throw RuntimeException("Error enviando correo", ex)
        }
    }

    private fun buildOtpBody(codigo: String): String {
        return """
            NordTafl

            Tu codigo de verificacion es:

                $codigo

            Este codigo expira en 5 minutos.
            Si no solicitaste esto, ignora este correo.

            --
            NordTafl - Ajedrez Vikingo
        """.trimIndent()
    }

    private fun buildPasswordResetBody(token: String): String {
        return """
            NordTafl

            Recibimos una solicitud para restablecer tu contrasena.

            Usa el siguiente codigo en la app:

                $token

            Este codigo expira en 10 minutos.
            Si no solicitaste esto, ignora este correo.

            --
            NordTafl - Ajedrez Vikingo
        """.trimIndent()
    }
}