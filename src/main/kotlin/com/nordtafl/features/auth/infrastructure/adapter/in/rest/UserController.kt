package com.nordtafl.features.auth.infrastructure.adapter.`in`.rest

import com.nordtafl.features.auth.domain.port.`in`.GetProfileCommand
import com.nordtafl.features.auth.domain.port.`in`.GetProfileUseCase
import com.nordtafl.features.auth.domain.port.`in`.UploadAvatarCommand
import com.nordtafl.features.auth.domain.port.`in`.UploadAvatarUseCase
import com.nordtafl.features.auth.infrastructure.adapter.`in`.rest.dto.UploadAvatarResponse
import com.nordtafl.features.auth.infrastructure.adapter.`in`.rest.dto.UserResponse
import com.nordtafl.shared.config.MetaFactory
import com.nordtafl.shared.dto.ApiResponse
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile
import java.util.UUID

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val getProfileUseCase: GetProfileUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase,
    private val metaFactory: MetaFactory
) {

    private val log = LoggerFactory.getLogger(UserController::class.java)

    @GetMapping("/me")
    fun getProfile(
        @RequestAttribute("userId") userId: UUID
    ): ResponseEntity<ApiResponse<UserResponse>> {
        log.info("GET /api/v1/users/me")

        val user = getProfileUseCase.getProfile(GetProfileCommand(userId = userId))

        return ResponseEntity.ok(
            ApiResponse.success(
                UserResponse(
                    id = user.id.toString(),
                    nombre = user.nombre,
                    correo = user.correo.value,
                    rol = user.rol.name.lowercase(),
                    estado = user.estado.name.lowercase(),
                    emailVerificado = user.emailVerificado,
                    avatarUrl = user.avatarUrl,
                    ultimoAcceso = user.ultimoAcceso,
                    creadoEn = user.createdAt
                ),
                metaFactory.build()
            )
        )
    }

    @PostMapping("/me/avatar", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    fun uploadAvatar(
        @RequestAttribute("userId") userId: UUID,
        @RequestParam("file") file: MultipartFile
    ): ResponseEntity<ApiResponse<UploadAvatarResponse>> {
        log.info("POST /api/v1/users/me/avatar")

        val result = uploadAvatarUseCase.uploadAvatar(
            UploadAvatarCommand(
                userId = userId,
                fileName = file.originalFilename ?: "avatar",
                contentType = file.contentType ?: "application/octet-stream",
                fileBytes = file.bytes
            )
        )

        return ResponseEntity.ok(
            ApiResponse.success(
                UploadAvatarResponse(avatarUrl = result.avatarUrl, mensaje = result.mensaje),
                metaFactory.build()
            )
        )
    }
}