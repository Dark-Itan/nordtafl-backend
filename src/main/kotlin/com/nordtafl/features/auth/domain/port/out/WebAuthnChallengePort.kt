package com.nordtafl.features.auth.domain.port.out

import java.time.Duration
import java.util.UUID

interface WebAuthnChallengePort {

    fun storeRegistrationChallenge(userId: UUID, challenge: String, ttl: Duration)

    fun getRegistrationChallenge(userId: UUID): String?

    fun deleteRegistrationChallenge(userId: UUID)

    fun storeLoginChallenge(correo: String, challenge: String, ttl: Duration)

    fun getLoginChallenge(correo: String): String?

    fun deleteLoginChallenge(correo: String)
}