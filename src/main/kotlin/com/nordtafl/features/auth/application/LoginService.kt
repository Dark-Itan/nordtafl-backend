package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.Password
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.LoginCommand
import com.nordtafl.features.auth.domain.port.`in`.LoginResult
import com.nordtafl.features.auth.domain.port.`in`.LoginUseCase
import com.nordtafl.features.auth.domain.port.out.PasswordHasherPort
import com.nordtafl.features.auth.domain.port.out.TokenGeneratorPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ForbiddenException
import com.nordtafl.shared.exception.UnauthorizedException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class LoginService(
    private val userRepository: UserRepositoryPort,
    private val passwordHasher: PasswordHasherPort,
    private val tokenGenerator: TokenGeneratorPort
) : LoginUseCase {

    private val log = LoggerFactory.getLogger(LoginService::class.java)

    @Transactional
    override fun login(command: LoginCommand): LoginResult {
        log.info("Intento de login para correo: ${command.correo}")

        val email = Email(command.correo)
        val password = Password(command.password)

        val user = userRepository.findByEmail(email)
            ?: throw UnauthorizedException(
                code = "INVALID_CREDENTIALS",
                message = "Credenciales inválidas"
            )

        if (!passwordHasher.verify(password, user.passwordHash)) {
            log.warn("Login fallido para correo: ${command.correo}")
            throw UnauthorizedException(
                code = "INVALID_CREDENTIALS",
                message = "Credenciales inválidas"
            )
        }

        when (user.estado) {
            UserStatus.BLOQUEADO -> throw ForbiddenException(
                code = "ACCOUNT_LOCKED",
                message = "La cuenta está bloqueada"
            )
            UserStatus.ELIMINADO -> throw ForbiddenException(
                code = "ACCOUNT_DELETED",
                message = "La cuenta ha sido eliminada"
            )
            UserStatus.PENDIENTE_VERIFICACION -> throw ForbiddenException(
                code = "ACCOUNT_NOT_VERIFIED",
                message = "La cuenta no ha sido verificada"
            )
            UserStatus.ACTIVO -> { /* OK */ }
        }

        val tokens = tokenGenerator.generateTokens(user)
        log.info("Login exitoso para usuario: ${user.id}")

        return LoginResult(
            user = user,
            accessToken = tokens.accessToken,
            refreshToken = tokens.refreshToken,
            accessTokenExpiresIn = tokens.accessTokenExpiresIn
        )
    }
}