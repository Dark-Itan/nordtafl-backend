package com.nordtafl.shared.exception

class ForbiddenException(
    code: String = "FORBIDDEN",
    message: String = "No tienes permiso para esta acción"
) : DomainException(code, message)