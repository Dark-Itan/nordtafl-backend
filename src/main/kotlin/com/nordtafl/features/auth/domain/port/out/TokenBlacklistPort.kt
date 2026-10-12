package com.nordtafl.features.auth.domain.port.out

import java.time.Duration

interface TokenBlacklistPort {

    fun add(token: String, ttl: Duration)

    fun contains(token: String): Boolean
}