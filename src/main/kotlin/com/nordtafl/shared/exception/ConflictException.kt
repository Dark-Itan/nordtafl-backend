package com.nordtafl.shared.exception

class ConflictException(
    code: String,
    message: String
) : DomainException(code, message)