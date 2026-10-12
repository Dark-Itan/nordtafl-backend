package com.nordtafl.shared.config

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class StartupLogger(
    private val appProperties: AppProperties,
    private val environment: Environment
) {

    private val log = LoggerFactory.getLogger(StartupLogger::class.java)

    @EventListener(ApplicationReadyEvent::class)
    fun onApplicationReady() {
        val port = environment.getProperty("server.port") ?: "8080"
        val env = appProperties.environment


        log.info("  ${appProperties.name}")
        log.info("  API corriendo en http://localhost:$port")
        log.info("  Ambiente: $env")
    }
}