package com.nordtafl.features.auth.infrastructure.adapter.out.cloudinary

import com.cloudinary.Cloudinary
import com.cloudinary.utils.ObjectUtils
import com.nordtafl.features.auth.domain.port.out.CloudinaryPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CloudinaryAdapter(
    @Value("\${app.cloudinary.cloud-name}") private val cloudName: String,
    @Value("\${app.cloudinary.api-key}") private val apiKey: String,
    @Value("\${app.cloudinary.api-secret}") private val apiSecret: String
) : CloudinaryPort {

    private val log = LoggerFactory.getLogger(CloudinaryAdapter::class.java)

    private val cloudinary: Cloudinary by lazy {
        Cloudinary(ObjectUtils.asMap(
            "cloud_name", cloudName,
            "api_key", apiKey,
            "api_secret", apiSecret,
            "secure", true
        ))
    }

    override fun uploadAvatar(
        userId: String,
        fileName: String,
        contentType: String,
        bytes: ByteArray
    ): String {
        log.info("Subiendo avatar a Cloudinary para usuario: $userId")

        val options = ObjectUtils.asMap(
            "public_id", "nordtafl/avatars/$userId",
            "folder", "nordtafl/avatars",
            "resource_type", "image",
            "overwrite", true,
            "invalidate", true,
            "transformation", "w_400,h_400,c_fill,g_face,q_auto,f_auto"
        )

        val result = cloudinary.uploader().upload(bytes, options)

        val url = result["secure_url"] as? String
            ?: throw IllegalStateException("Cloudinary no devolvió secure_url")

        log.info("Avatar subido exitosamente. URL: $url")

        return url
    }

    override fun deleteAvatar(userId: String) {
        log.info("Eliminando avatar de Cloudinary para usuario: $userId")

        val publicId = "nordtafl/avatars/$userId"

        try {
            cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap())
            log.debug("Avatar eliminado: $publicId")
        } catch (ex: Exception) {
            log.warn("Error eliminando avatar $publicId: ${ex.message}")
        }
    }
}