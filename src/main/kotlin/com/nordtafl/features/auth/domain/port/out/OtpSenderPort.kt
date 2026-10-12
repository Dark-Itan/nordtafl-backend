package com.nordtafl.features.auth.domain.port.out

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.port.`in`.OtpPurpose

interface OtpSenderPort {

    fun sendOtp(
        email: Email,
        codigo: String,
        proposito: OtpPurpose
    )
}