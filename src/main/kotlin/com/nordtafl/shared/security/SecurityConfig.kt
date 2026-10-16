package com.nordtafl.shared.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val unauthorizedEntryPoint: UnauthorizedEntryPoint
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .csrf { it.disable() }
            .cors { }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { it.authenticationEntryPoint(unauthorizedEntryPoint) }
            .authorizeHttpRequests { auth ->
                // Endpoints públicos
                auth.requestMatchers("/", "/actuator/**").permitAll()
                auth.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                // Auth públicos
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/register").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/logout").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/otp/request").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/otp/verify").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/token/refresh").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/password/forgot").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/password/reset").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/google").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/webauthn/login/challenge").permitAll()
                auth.requestMatchers(HttpMethod.POST, "/api/v1/auth/webauthn/login/verify").permitAll()
                auth.requestMatchers(HttpMethod.GET, "/api/v1/leaderboard").permitAll()

                // Todo lo demás requiere autenticación
                auth.anyRequest().authenticated()
            }
            .httpBasic { it.disable() }
            .formLogin { it.disable() }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }
}