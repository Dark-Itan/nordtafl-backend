package com.nordtafl.features.auth.domain.port.`in`

import com.nordtafl.features.auth.domain.model.User

data class RegisterUserCommand(
    val nombre: String,
    val correo: String,
    val password: String,
    val rol: String = "jugador"
)

interface RegisterUserUseCase {

    fun register(command: RegisterUserCommand): User
}