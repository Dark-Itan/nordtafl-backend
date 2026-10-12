package com.nordtafl.shared.dto

import java.time.Instant

data class Meta(
    val timestamp: Instant,
    val correlationId: String,
    val environment: String
)