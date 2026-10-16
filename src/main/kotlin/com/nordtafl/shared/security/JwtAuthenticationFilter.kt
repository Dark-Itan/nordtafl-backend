package com.nordtafl.shared.security

import com.nordtafl.features.auth.domain.port.out.TokenBlacklistPort
import com.nordtafl.features.auth.domain.port.out.TokenGeneratorPort
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class JwtAuthenticationFilter(
    private val tokenGenerator: TokenGeneratorPort,
    private val tokenBlacklist: TokenBlacklistPort
) : OncePerRequestFilter() {

    private val log = LoggerFactory.getLogger(JwtAuthenticationFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader.isNullOrBlank() || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response)
            return
        }

        val token = authHeader.substring(7)

        try {
            if (tokenBlacklist.contains(token)) {
                log.warn("Token en blacklist")
                filterChain.doFilter(request, response)
                return
            }

            val userId = tokenGenerator.validateAccessToken(token)

            if (userId != null) {
                val authentication = UsernamePasswordAuthenticationToken(
                    userId.toString(),
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER"))
                )

                SecurityContextHolder.getContext().authentication = authentication
                request.setAttribute("userId", userId)

                log.debug("Usuario autenticado: $userId")
            } else {
                log.debug("Token inválido")
            }
        } catch (ex: Exception) {
            log.warn("Error procesando token: ${ex.message}")
        }

        filterChain.doFilter(request, response)
    }
}