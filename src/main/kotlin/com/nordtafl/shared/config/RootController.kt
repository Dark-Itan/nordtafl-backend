package com.nordtafl.shared.config

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class RootController(
    private val appProperties: AppProperties
) {

    @GetMapping("/")
    fun root(): Map<String, String> = mapOf(
        "name" to appProperties.name,
        "description" to appProperties.description,
        "version" to appProperties.version,
        "status" to "running"
    )
}