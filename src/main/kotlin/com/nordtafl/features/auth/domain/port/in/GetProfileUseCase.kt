package com.nordtafl.features.auth.domain.port.`in`

import com.nordtafl.features.auth.domain.model.User
import java.util.UUID

data class GetProfileCommand(
    val userId: UUID
)

interface GetProfileUseCase {

    fun getProfile(command: GetProfileCommand): User
}