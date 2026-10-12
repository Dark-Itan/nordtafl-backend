package com.nordtafl.features.auth.domain.port.out

data class GoogleUserInfo(
    val sub: String,
    val email: String,
    val emailVerified: Boolean,
    val name: String,
    val picture: String?
)

interface GoogleTokenValidatorPort {

    fun validateIdToken(idToken: String): GoogleUserInfo
}