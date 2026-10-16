package com.nordtafl.features.auth.infrastructure.adapter.out.google

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.nordtafl.features.auth.domain.port.out.GoogleTokenValidatorPort
import com.nordtafl.features.auth.domain.port.out.GoogleUserInfo
import com.nordtafl.shared.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Collections

@Component
class GoogleTokenValidatorAdapter(
    @Value("\${app.google.client-id}") private val clientId: String
) : GoogleTokenValidatorPort {

    private val log = LoggerFactory.getLogger(GoogleTokenValidatorAdapter::class.java)

    private val verifier: GoogleIdTokenVerifier by lazy {
        GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance())
            .setAudience(Collections.singletonList(clientId))
            .setIssuer("https://accounts.google.com")
            .build()
    }

    override fun validateIdToken(idToken: String): GoogleUserInfo {
        log.debug("Validando ID Token de Google")

        val token: GoogleIdToken? = try {
            verifier.verify(idToken)
        } catch (ex: Exception) {
            log.warn("Error validando ID Token: ${ex.message}")
            throw UnauthorizedException(
                code = "GOOGLE_TOKEN_INVALID",
                message = "El ID Token de Google es inválido"
            )
        }

        if (token == null) {
            log.warn("ID Token de Google inválido")
            throw UnauthorizedException(
                code = "GOOGLE_TOKEN_INVALID",
                message = "El ID Token de Google es inválido"
            )
        }

        val payload = token.payload

        val sub = payload.subject
        val email = payload.email
        val emailVerified = payload.emailVerified ?: false
        val name = payload["name"] as? String ?: email
        val picture = payload["picture"] as? String

        if (sub.isNullOrBlank() || email.isNullOrBlank()) {
            log.warn("ID Token sin sub o email")
            throw UnauthorizedException(
                code = "GOOGLE_TOKEN_INVALID",
                message = "El ID Token no contiene la información requerida"
            )
        }

        log.debug("ID Token válido para email: $email")

        return GoogleUserInfo(
            sub = sub,
            email = email,
            emailVerified = emailVerified,
            name = name,
            picture = picture
        )
    }
}