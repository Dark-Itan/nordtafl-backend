package com.nordtafl.shared.dto

import java.time.Instant

data class ErrorResponse(
    val type: String,
    val title: String,
    val status: Int,
    val detail: String,
    val instance: String,
    val code: String,
    val timestamp: Instant,
    val correlationId: String
)