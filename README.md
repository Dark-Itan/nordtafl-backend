# NordTafl Backend

Backend del proyecto **NordTafl** — juego de Hnefatafl multijugador por turnos, con asistente de juego para principiantes basado en heurísticas explicables.

## Stack

- **Java 21** (Azul Zulu)
- **Kotlin**
- **Spring Boot 3.3.x**
- **Gradle** (Kotlin DSL)
- **PostgreSQL 17**
- **Redis**
- **Arquitectura hexagonal** + Feature-First

## Estructura

- `features/` — features del dominio
  - `auth/` — autenticación, registro, login, OTP, WebAuthn
  - `game/` — partidas, movimientos, ELO, matchmaking
  - `advisor/` — asistente Minimax
  - `profile/` — preferencias y estadísticas
- `shared/` — código compartido
  - `config/` — configuración global
  - `security/` — JWT, filtros, BCrypt
  - `dto/` — DTOs comunes
  - `exception/` — manejo de errores

## Estado

En desarrollo.
