package com.nordtafl.features.auth.application

import com.nordtafl.features.auth.domain.port.`in`.UploadAvatarCommand
import com.nordtafl.features.auth.domain.port.`in`.UploadAvatarResult
import com.nordtafl.features.auth.domain.port.`in`.UploadAvatarUseCase
import com.nordtafl.features.auth.domain.port.out.CloudinaryPort
import com.nordtafl.features.auth.domain.port.out.UserRepositoryPort
import com.nordtafl.shared.exception.ResourceNotFoundException
import com.nordtafl.shared.exception.ValidationException
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UploadAvatarService(
    private val userRepository: UserRepositoryPort,
    private val cloudinary: CloudinaryPort
) : UploadAvatarUseCase {

    private val log = LoggerFactory.getLogger(UploadAvatarService::class.java)

    @Transactional
    override fun uploadAvatar(command: UploadAvatarCommand): UploadAvatarResult {
        log.info("Subiendo avatar para usuario: ${command.userId}")

        validateImage(command.contentType, command.fileBytes.size.toLong())

        val user = userRepository.findById(command.userId)
            ?: throw ResourceNotFoundException(
                resource = "Usuario",
                id = command.userId.toString()
            )

        val avatarUrl = cloudinary.uploadAvatar(
            userId = command.userId.toString(),
            fileName = command.fileName,
            contentType = command.contentType,
            bytes = command.fileBytes
        )

        val updatedUser = user.copy(avatarUrl = avatarUrl)
        userRepository.update(updatedUser)

        log.info("Avatar actualizado para usuario: ${command.userId}")

        return UploadAvatarResult(
            avatarUrl = avatarUrl,
            mensaje = "Avatar actualizado correctamente."
        )
    }

    private fun validateImage(contentType: String, size: Long) {
        val allowedTypes = listOf("image/jpeg", "image/png", "image/webp")
        if (contentType !in allowedTypes) {
            throw ValidationException(
                code = "INVALID_AVATAR_FORMAT",
                message = "Formato no permitido. Usa JPG, PNG o WebP.",
                field = "avatar"
            )
        }

        val maxSize = 5 * 1024 * 1024L  // ← Long explícito (sufijo L)
        if (size > maxSize) {
            throw ValidationException(
                code = "AVATAR_TOO_LARGE",
                message = "El avatar no puede superar los 5 MB.",
                field = "avatar"
            )
        }

        if (size == 0L) {
            throw ValidationException(
                code = "AVATAR_EMPTY",
                message = "El archivo está vacío.",
                field = "avatar"
            )
        }
    }
}