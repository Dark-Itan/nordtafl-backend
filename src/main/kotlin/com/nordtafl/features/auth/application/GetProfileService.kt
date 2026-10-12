package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.model.User
import com.nordtafl.features.auth.domain.port.`in`.GetProfileCommand
import com.nordtafl.features.auth.domain.port.`in`.GetProfileUseCase
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GetProfileService(
    private val userRepository: UserRepositoryPort
) : GetProfileUseCase {

    private val log = LoggerFactory.getLogger(GetProfileService::class.java)

    @Transactional(readOnly = true)
    override fun getProfile(command: GetProfileCommand): User {
        log.info("Consultando perfil del usuario: ${command.userId}")

        return userRepository.findById(command.userId)
            ?: throw ResourceNotFoundException(
                resource = "Usuario",
                id = command.userId.toString()
            )
    }
}