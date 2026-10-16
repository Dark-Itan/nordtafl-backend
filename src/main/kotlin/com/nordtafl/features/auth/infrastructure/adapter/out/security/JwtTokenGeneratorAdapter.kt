package com.nordtafl.features.auth.infrastructure.adapter.out.security

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.port.out.GeneratedTokens
import com.nordtafl.features.auth.domain.port.out.TokenGeneratorPort
import com.nordtafl.shared.config.AppProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.nio.charset.StandardCharsets
import java.security.SecureRandom
import java.time.Instant
import java.util.Base64
import java.util.Date
import java.util.UUID
import javax.crypto.SecretKey

@Component
class JwtTokenGeneratorAdapter(
    private val appProperties: AppProperties
) : TokenGeneratorPort {

    private val log = LoggerFactory.getLogger(JwtTokenGeneratorAdapter::class.java)
    private val secureRandom = SecureRandom()

    private val signingKey: SecretKey by lazy {
        val secretBytes = appProperties.jwt.secret.toByteArray(StandardCharsets.UTF_8)
        Keys.hmacShaKeyFor(secretBytes)
    }

    override fun generateTokens(user: User): GeneratedTokens {
        requireNotNull(user.id) { "El usuario debe tener un ID para generar tokens" }

        val accessToken = generateAccessToken(user)
        val refreshToken = generateRefreshToken()

        log.debug("Tokens generados para usuario: ${user.id}")

        return GeneratedTokens(
            accessToken = accessToken,
            refreshToken = refreshToken,
            accessTokenExpiresIn = appProperties.jwt.accessExpiration,
            refreshTokenExpiresIn = appProperties.jwt.refreshExpiration
        )
    }

    override fun validateAccessToken(token: String): UUID? {
        return try {
            val claims = parseClaims(token)
            extractUserIdFromClaims(claims)
        } catch (ex: JwtException) {
            log.debug("Token inválido: ${ex.message}")
            null
        } catch (ex: IllegalArgumentException) {
            log.debug("Token mal formado: ${ex.message}")
            null
        }
    }

    override fun extractUserId(token: String): UUID? {
        return validateAccessToken(token)
    }

    private fun generateAccessToken(user: User): String {
        val now = Instant.now()
        val expiry = now.plusSeconds(appProperties.jwt.accessExpiration)

        return Jwts.builder()
            .subject(user.id.toString())
            .claim("email", user.correo.value)
            .claim("rol", user.rol.name)
            .issuedAt(Date.from(now))
            .expiration(Date.from(expiry))
            .signWith(signingKey)
            .compact()
    }

    private fun generateRefreshToken(): String {
        val bytes = ByteArray(64)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun parseClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }

    private fun extractUserIdFromClaims(claims: Claims): UUID? {
        val subject = claims.subject ?: return null
        return try {
            UUID.fromString(subject)
        } catch (ex: IllegalArgumentException) {
            log.warn("Subject inválido en token: $subject")
            null
        }
    }
}