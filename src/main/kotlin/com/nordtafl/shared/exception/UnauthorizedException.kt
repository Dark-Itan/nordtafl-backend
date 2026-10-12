package com.nordtafl.shared.exception

class UnauthorizedException(
    code: String = "UNAUTHORIZED",
    message: String = "No autorizado"
) : DomainException(code, message)