package com.nordtafl.features.auth.infrastructure.adapter.out.redis

import com.nordtafl.features.auth.domain.port.out.WebAuthnChallengePort
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class WebAuthnChallengeRedisAdapter(
    private val redisTemplate: StringRedisTemplate
) : WebAuthnChallengePort {

    private val log = LoggerFactory.getLogger(WebAuthnChallengeRedisAdapter::class.java)

    override fun storeRegistrationChallenge(userId: UUID, challenge: String, ttl: Duration) {
        val key = regKey(userId)
        redisTemplate.opsForValue().set(key, challenge, ttl)
        log.debug("Challenge de registro guardado para usuario: $userId")
    }

    override fun getRegistrationChallenge(userId: UUID): String? {
        return redisTemplate.opsForValue().get(regKey(userId))
    }

    override fun deleteRegistrationChallenge(userId: UUID) {
        redisTemplate.delete(regKey(userId))
        log.debug("Challenge de registro eliminado para usuario: $userId")
    }

    override fun storeLoginChallenge(correo: String, challenge: String, ttl: Duration) {
        val key = loginKey(correo)
        redisTemplate.opsForValue().set(key, challenge, ttl)
        log.debug("Challenge de login guardado para correo: $correo")
    }

    override fun getLoginChallenge(correo: String): String? {
        return redisTemplate.opsForValue().get(loginKey(correo))
    }

    override fun deleteLoginChallenge(correo: String) {
        redisTemplate.delete(loginKey(correo))
        log.debug("Challenge de login eliminado para correo: $correo")
    }

    private fun regKey(userId: UUID) = "webauthn:reg:$userId"

    private fun loginKey(correo: String) = "webauthn:login:$correo"
}