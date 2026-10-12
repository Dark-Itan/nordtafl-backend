package com.nordtafl.features.auth.domain.port.out

import com.nordtafl.features.auth.domain.model.value.Password

interface PasswordHasherPort {

    fun hash(password: Password): String

    fun verify(rawPassword: Password, hashedPassword: String): Boolean
}