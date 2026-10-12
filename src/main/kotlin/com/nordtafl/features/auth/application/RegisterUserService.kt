package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.model.value.Email
import com.nordtafl.features.auth.domain.model.value.Password
import com.nordtafl.features.auth.domain.model.value.UserRole
import com.nordtafl.features.auth.domain.model.value.UserStatus
import com.nordtafl.features.auth.domain.port.`in`.RegisterUserCommand
import com.nordtafl.features.auth.domain.port.`in`.RegisterUserUseCase
import com.nordtafl.features.auth.domain.port.out.PasswordHasherPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ConflictException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class RegisterUserService(
    private val userRepository: UserRepositoryPort,
    private val passwordHasher: PasswordHasherPort
) : RegisterUserUseCase {

    private val log = LoggerFactory.getLogger(RegisterUserService::class.java)

    @Transactional
    override fun register(command: RegisterUserCommand): User {
        log.info("Registrando usuario con correo: ${command.correo}")

        val email = Email(command.correo)
        val password = Password(command.password)

        if (userRepository.existsByEmail(email)) {
            log.warn("Intento de registro con correo duplicado: ${command.correo}")
            throw ConflictException(
                code = "USER_ALREADY_EXISTS",
                message = "El correo '${command.correo}' ya está registrado"
            )
        }

        val passwordHash = passwordHasher.hash(password)

        val user = User(
            id = null,
            nombre = command.nombre,
            correo = email,
            passwordHash = passwordHash,
            rol = parseRole(command.rol),
            estado = UserStatus.PENDIENTE_VERIFICACION,
            emailVerificado = false
        )

        val savedUser = userRepository.save(user)
        log.info("Usuario registrado exitosamente con ID: ${savedUser.id}")

        return savedUser
    }

    private fun parseRole(rol: String): UserRole {
        return when (rol.lowercase()) {
            "jugador" -> UserRole.JUGADOR
            "admin" -> UserRole.ADMIN
            else -> UserRole.JUGADOR
        }
    }
}