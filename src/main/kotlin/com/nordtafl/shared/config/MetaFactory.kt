package com.nordtafl.shared.config

import com.nordtafl.shared.dto.Meta
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.UUID

@Component
class MetaFactory(
    private val appProperties: AppProperties
) {

    fun build(): Meta {
        return Meta(
            timestamp = Instant.now(),
            correlationId = UUID.randomUUID().toString(),
            environment = appProperties.environment
        )
    }
}