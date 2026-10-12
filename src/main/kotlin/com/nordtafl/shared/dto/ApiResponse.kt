package com.nordtafl.shared.dto

data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val meta: Meta
) {
    companion object {
        fun <T> success(data: T, meta: Meta): ApiResponse<T> =
            ApiResponse(true, data, meta)

        fun <T> error(meta: Meta): ApiResponse<T> =
            ApiResponse(false, null, meta)
    }
}