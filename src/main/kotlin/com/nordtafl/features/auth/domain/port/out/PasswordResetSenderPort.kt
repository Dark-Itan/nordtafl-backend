package com.nordtafl.features.auth.domain.port.out

import com.nordtafl.features.auth.domain.model.value.Email

interface PasswordResetSenderPort {

    fun sendPasswordResetToken(
        email: Email,
        token: String
    )
}