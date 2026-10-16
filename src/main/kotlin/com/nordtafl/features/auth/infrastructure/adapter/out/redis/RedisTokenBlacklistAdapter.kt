package com.nordtafl.features.auth.infrastructure.adapter.out.redis

import com.nordtafl.features.auth.domain.port.out.TokenBlacklistPort
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RedisTokenBlacklistAdapter(
    private val redisTemplate: StringRedisTemplate
) : TokenBlacklistPort {

    private val log = LoggerFactory.getLogger(RedisTokenBlacklistAdapter::class.java)

    override fun add(token: String, ttl: Duration) {
        val key = buildKey(token)
        log.debug("Agregando token a blacklist con TTL de ${ttl.seconds}s")

        redisTemplate.opsForValue().set(key, "revoked", ttl)
    }

    override fun contains(token: String): Boolean {
        val key = buildKey(token)
        val exists = redisTemplate.hasKey(key)

        log.debug("Verificando token en blacklist: ${exists == true}")

        return exists == true
    }

    private fun buildKey(token: String): String {
        return "blacklist:jwt:$token"
    }
}