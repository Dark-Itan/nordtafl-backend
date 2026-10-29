plugins {
    kotlin("jvm") version "2.0.20"
    kotlin("plugin.spring") version "2.0.20"
    kotlin("plugin.jpa") version "2.0.20"
    jacoco
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.sonarqube") version "7.5.0.8588"

}


extra["hibernate.version"] = "6.6.1.Final"

group = "com.nordtafl"
version = "1.0.0"
description = "nordtafl-backend"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        csv.required.set(false)
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco"))
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

dependencies {
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-actuator")


    // PostgreSQL + HikariCP
    runtimeOnly("org.postgresql:postgresql")
    implementation("com.zaxxer:HikariCP")

    // JWT
    implementation("io.jsonwebtoken:jjwt-api:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")

    // WebAuthn (Yubico)
    implementation("com.yubico:webauthn-server-core:2.5.0")

    // Kotlin
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.springframework.boot:spring-boot-starter-mail")
    implementation("com.google.api-client:google-api-client:2.7.0")
    implementation("com.cloudinary:cloudinary-http44:1.39.0")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    // Tests
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    // MockK para Kotlin
    testImplementation("io.mockk:mockk:1.13.13")
    testImplementation("org.assertj:assertj-core:3.26.3")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    archiveFileName.set("nordtafl.jar")
}

sonar {
    properties {
        property("sonar.projectKey", "nordtafl-backend")
        property("sonar.projectName", "NordTafl Backend")
        property("sonar.projectVersion", "1.0.0")
        property("sonar.host.url", "http://localhost:9000")
        property("sonar.coverage.jacoco.xmlReportPaths", "${layout.buildDirectory.get()}/reports/jacoco/jacocoTestReport.xml")
    }
}