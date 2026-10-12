package com.nordtafl.shared.exception

abstract class DomainException(
    val code: String,
    override val message: String
) : RuntimeException(message)