package com.nordtafl.shared.exception

class ResourceNotFoundException(
    resource: String,
    id: String
) : DomainException(
    code = "RESOURCE_NOT_FOUND",
    message = "El recurso $resource con id '$id' no existe"
)