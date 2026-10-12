package com.nordtafl.shared.exception

class ValidationException(
    code: String,
    message: String,
    val field: String? = null
) : DomainException(code, message)