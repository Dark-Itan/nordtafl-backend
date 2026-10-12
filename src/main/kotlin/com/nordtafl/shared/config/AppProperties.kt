package com.nordtafl.shared.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "app")
data class AppProperties(
    val name: String,
    val description: String,
    val version: String,
    val environment: String,
    val jwt: Jwt,
    val webauthn: WebAuthn
) {
    data class Jwt(
        val secret: String,
        val accessExpiration: Long,
        val refreshExpiration: Long
    )

    data class WebAuthn(
        val rpId: String,
        val rpName: String,
        val origin: String
    )
}