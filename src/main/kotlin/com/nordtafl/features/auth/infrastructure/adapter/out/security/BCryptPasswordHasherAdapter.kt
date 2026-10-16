package com.nordtafl.features.auth.infrastructure.adapter.out.security

import com.nordtafl.features.auth.domain.model.value.Password
import com.nordtafl.features.auth.domain.port.out.PasswordHasherPort
import org.slf4j.LoggerFactory
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component

@Component
class BCryptPasswordHasherAdapter : PasswordHasherPort {

    private val log = LoggerFactory.getLogger(BCryptPasswordHasherAdapter::class.java)
    private val encoder = BCryptPasswordEncoder(BCRYPT_STRENGTH)

    override fun hash(password: Password): String {
        log.debug("Hasheando password con BCrypt")
        return encoder.encode(password.value)
    }

    override fun verify(rawPassword: Password, hashedPassword: String): Boolean {
        log.debug("Verificando password con BCrypt")
        return encoder.matches(rawPassword.value, hashedPassword)
    }

    companion object {
        private const val BCRYPT_STRENGTH = 12
    }
}