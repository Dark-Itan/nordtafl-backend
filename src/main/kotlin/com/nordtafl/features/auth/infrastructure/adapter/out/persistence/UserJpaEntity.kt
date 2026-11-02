package com.nordtafl.features.auth.infrastructure.adapter.out.persistence

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "users", schema = "auth")
class UserJpaEntity(

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    var id: UUID? = null,

    @Column(name = "nombre", nullable = false, length = 100)
    var nombre: String = "",

    @Column(name = "correo", nullable = false, unique = true, length = 254)
    var correo: String = "",

    @Column(name = "password_hash", nullable = false, length = 255)
    var passwordHash: String = "",

    @Column(name = "rol", nullable = false, length = 20)
    var rol: String = "jugador",

    @Column(name = "estado", nullable = false, length = 30)
    var estado: String = "pendiente_verificacion",

    @Column(name = "email_verificado", nullable = false)
    var emailVerificado: Boolean = false,

    @Column(name = "avatar_url")
    var avatarUrl: String? = null,

    @Column(name = "google_id")
    var googleId: String? = null,

    @Column(name = "ultimo_acceso")
    var ultimoAcceso: Instant? = null,

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: Instant? = null,

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant? = null
)