package com.nordtafl

import com.nordtafl.shared.config.AppProperties
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
@EnableConfigurationProperties(AppProperties::class)
class NordtaflBackendApplication

fun main(args: Array<String>) {
    runApplication<NordtaflBackendApplication>(*args)
}