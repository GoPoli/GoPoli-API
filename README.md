# GoPoli-API

## Estado del Proyecto

[![Java CI with Maven](https://github.com/GoPoli/GoPoli-API/actions/workflows/maven.yml/badge.svg)](https://github.com/GoPoli/GoPoli-API/actions/workflows/maven.yml)
[![CodeQL Advanced](https://github.com/GoPoli/GoPoli-API/actions/workflows/codeql.yml/badge.svg)](https://github.com/GoPoli/GoPoli-API/actions/workflows/codeql.yml)
[![Publish Package to GHCR](https://github.com/GoPoli/GoPoli-API/actions/workflows/packaging.yml/badge.svg)](https://github.com/GoPoli/GoPoli-API/actions/workflows/packaging.yml)
[![Dependabot Updates](https://github.com/GoPoli/GoPoli-API/actions/workflows/dependabot/dependabot-updates/badge.svg)](https://github.com/GoPoli/GoPoli-API/actions/workflows/dependabot/dependabot-updates)

## Descripción

GoPoli-API es la API REST de **GoPoli**, la plataforma de viajes compartidos entre estudiantes del Politécnico Colombiano Jaime Isaza Cadavid. Está desarrollada en **Java 17** con **Spring Boot 4**, persiste en **PostgreSQL** mediante **Spring Data JPA** y autentica con **JWT**. Gestiona cuentas, catálogo académico, viajes compartidos, chat de grupo, agenda de rutas habituales y registro de conductores.

La API es consumida por la PWA [GoPoli-Web](https://github.com/GoPoli/GoPoli-Web) y usa la base de datos de [GoPoli-DB](https://github.com/GoPoli/GoPoli-DB), que es la dueña del esquema: la API solo lo valida al arrancar.

## Tecnologías Principales

- **Java 17** - Lenguaje de programación
- **Spring Boot 4.0** - Framework principal (Web MVC)
- **Spring Data JPA / Hibernate** - Persistencia con validación de esquema
- **PostgreSQL 16** - Base de datos relacional
- **JWT (jjwt 0.12.6)** - Tokens de autenticación
- **Spring Security Crypto (BCrypt)** - Hash de contraseñas
- **Lombok** - Reducción de código repetitivo
- **Maven** - Gestión de dependencias y build
- **Docker** - Imagen por capas con usuario sin privilegios
- **GitHub Actions + GHCR** - Integración y publicación continua

## Estructura del Proyecto

```text
GoPoli-API/
├── .github/
│   ├── dependabot.yml                  # Actualización de dependencias (Maven, Docker, Actions)
│   └── workflows/
│       ├── codeql.yml                  # Análisis de seguridad CodeQL
│       ├── dependency-review.yml       # Revisión de dependencias en PRs
│       ├── maven.yml                   # Build y pruebas con Maven
│       ├── packaging.yml               # Publicación de la imagen en GHCR (SBOM y provenance)
│       └── stale.yml                   # Limpieza de issues y PRs inactivos
│
├── docs/
│   └── DEPLOY_RAILWAY.md               # Guía de despliegue en Railway
│
├── src/
│   ├── main/
│   │   ├── java/com/gopoli/api/
│   │   │   ├── config/                 # CORS, reloj de la aplicación y coordenadas de ubicaciones
│   │   │   ├── controller/             # Auth, User, Catalog, Trip, Message, RecurringRoute, Health
│   │   │   ├── dto/                    # Contratos de entrada y salida (records)
│   │   │   ├── model/                  # Entidades JPA y constantes de negocio
│   │   │   ├── policy/                 # Reglas de perfil, viajes y vehículos
│   │   │   ├── repository/             # Repositorios Spring Data JPA
│   │   │   ├── security/               # JwtService, PasswordService
│   │   │   └── GoPoliApplication.java  # Clase principal
│   │   └── resources/
│   │       └── application.properties  # Configuración parametrizada por entorno
│   └── test/java/com/gopoli/api/       # Pruebas unitarias
│
├── .dockerignore
├── .env.example                        # Plantilla de variables de entorno
├── Dockerfile                          # Imagen multi-etapa por capas (Maven → JRE 17 Alpine)
├── LICENSE
├── mvnw / mvnw.cmd                     # Maven Wrapper
└── pom.xml
```

## Descripción de Módulos

### Controller

| Controlador | Responsabilidad |
| --- | --- |
| `AuthController` | Registro con correo institucional `@elpoli.edu.co` e inicio de sesión con emisión de JWT |
| `UserController` | Perfil del usuario autenticado, foto, registro de conductor, historial, inhabilitar y eliminar cuenta |
| `CatalogController` | Catálogos públicos de carreras y ubicaciones (con caché HTTP de 5 minutos) |
| `TripController` | Ciclo de vida de los viajes: crear, unirse, salir, iniciar, finalizar, cancelar y consultas |
| `MessageController` | Chat del grupo de cada viaje |
| `RecurringRouteController` | Rutas habituales de la agenda |
| `HealthController` | Estado de la API y de la conexión a la base |

### Model

Entidades JPA: `User`, `Vehicle`, `Program`, `Location`, `Trip`, `TripMember`, `Message` y `RecurringRoute`, mapeadas a las tablas de GoPoli-DB. Las constantes de negocio viven en `GoPoliConstants`:

| Concepto | Valores |
| --- | --- |
| Tipo de usuario | Pasajero `1`, Conductor `2` |
| Estado de usuario | Activo `2`, Inhabilitado `3` |
| Tipo de viaje | Grupo de pasajeros `1`, Grupo de conductor `3` |
| Estado del viaje | Activo `1`, Cancelado `2`, Finalizado `3`, En curso `4` |
| Rol en el grupo | `creator`, `member` |
| Participación | `passenger`, `driver` |

### Security

- `JwtService`: firma y valida tokens HMAC con `GOPOLI_JWT_SECRET` (mínimo 32 caracteres; la API no arranca con uno más corto). El `subject` del token es el id del usuario.
- `PasswordService`: hash BCrypt. Las contraseñas deben tener entre 8 caracteres y 72 bytes, el límite de BCrypt.

### Policy

Reglas de negocio sin dependencias de Spring, cubiertas por pruebas unitarias: perfil (`ProfilePolicy`: dominio del correo, nombre, teléfono), viajes (`TripPolicy`: transiciones de estado, capacidad de 2 a 4 personas, descripción de hasta 500 caracteres) y vehículo (`VehicleValidator`).

### Config

- `AppConfig`: reloj de la aplicación en la zona `APP_TIMEZONE` y política CORS a partir de `CORS_ALLOWED_ORIGINS`.
- `LocationCoordinatesSeeder`: al arrancar, completa las coordenadas de las ubicaciones conocidas (estaciones del metro y salidas del campus) si faltan en la base.

## Mapa de la API

Base local: `http://localhost:8080`. Todas las rutas, salvo las públicas, requieren el encabezado `Authorization: Bearer <token>`. Los mensajes de error se devuelven en español.

| Método | Ruta | Acceso |
| --- | --- | --- |
| `GET` | `/health` | Público |
| `POST` | `/register` | Público |
| `POST` | `/login` | Público |
| `GET` | `/programs` | Público |
| `GET` | `/locations` | Público |
| `GET` `PUT` `DELETE` | `/users/me` | JWT |
| `PUT` | `/users/me/photo` | JWT |
| `POST` `DELETE` | `/users/me/driver` | JWT |
| `GET` | `/users/me/trip-history` | JWT |
| `POST` | `/users/me/deactivate` | JWT |
| `POST` | `/trips` | JWT |
| `GET` | `/trips/active` | JWT |
| `GET` | `/trips/{tripId}` · `/trips/{tripId}/members` | JWT (miembros del viaje) |
| `POST` | `/trips/{tripId}/join` | JWT |
| `PUT` | `/trips/{tripId}/start` · `/trips/{tripId}/finish` · `/trips/{tripId}/cancel` | JWT (creador) |
| `DELETE` | `/trips/{tripId}/members/{userId}` | JWT (el propio miembro) |
| `GET` | `/users/{userId}/trips/active` · `/users/{userId}/trips/member` · `/users/{userId}/trips/in-progress` | JWT (solo el dueño) |
| `GET` `POST` | `/trips/{tripId}/messages` | JWT (miembros del viaje) |
| `GET` | `/users/{userId}/recurring-routes` | JWT (solo el dueño) |
| `POST` | `/recurring-routes` | JWT |
| `PUT` `DELETE` | `/recurring-routes/{routeId}` | JWT (solo el dueño) |

### Ejemplos

```bash
curl -X POST http://localhost:8080/register \
  -H "Content-Type: application/json" \
  -d '{"email":"nombre@elpoli.edu.co","password":"clave-segura","name":"Nombre Apellido","phone":"3001234567","programId":1}'

curl -X POST http://localhost:8080/login \
  -H "Content-Type: application/json" \
  -d '{"email":"nombre@elpoli.edu.co","password":"clave-segura"}'
```

`/login` responde `{"token": "...", "user": {...}}`. El objeto `user` incluye `id`, `email`, `name`, `phone`, `programId`, `statusId`, `userTypeId`, `rating`, `profilePhoto`, `driver` y `vehicle`.

## Guía de Instalación

### Requisitos Previos

- Java 17 o superior
- Maven 3.9+ (o el Maven Wrapper incluido)
- PostgreSQL 16 con el esquema de GoPoli (recomendado: la imagen de [GoPoli-DB](https://github.com/GoPoli/GoPoli-DB))
- Docker (opcional, para contenedorización)

### 1. Levantar la Base de Datos

```bash
docker run -d --name gopoli-db -p 127.0.0.1:5432:5432 \
  -e POSTGRES_PASSWORD=<DB_PASSWORD> \
  -e GOPOLI_SEED_DEMO=true \
  ghcr.io/gopoli/gopoli-db:latest
```

La imagen crea el esquema, carga los catálogos y, con `GOPOLI_SEED_DEMO=true`, las cuentas de demostración (contraseña `gopoli-local-dev`). Detalles en [GoPoli-DB](https://github.com/GoPoli/GoPoli-DB).

### 2. Ejecución Local

```bash
git clone https://github.com/GoPoli/GoPoli-API.git
cd GoPoli-API
cp .env.example .env
./mvnw spring-boot:run
```

En Windows usa `mvnw.cmd spring-boot:run`. La API lee `.env` desde la carpeta de trabajo (`spring.config.import`); define ahí `SPRING_DATASOURCE_PASSWORD` y `GOPOLI_JWT_SECRET`. El archivo está en `.gitignore`.

Comprobación:

```bash
curl http://localhost:8080/health
curl http://localhost:8080/locations
```

### 3. Construcción de la Imagen Docker

El `Dockerfile` es multi-etapa: compila con Maven, extrae el JAR por capas (dependencias, cargador y aplicación) para aprovechar la caché y genera una imagen `eclipse-temurin:17-jre-alpine` que corre con el usuario sin privilegios `10001`, con `HEALTHCHECK` sobre `/health`. No hace falta tener Java instalado.

```bash
docker build --platform linux/amd64 -t ghcr.io/gopoli/gopoli-api:latest .
```

### 4. Ejecución con Docker

```bash
docker run -d --name gopoli-api -p 127.0.0.1:8080:8080 --env-file .env \
  --read-only --tmpfs /tmp --cap-drop ALL --security-opt no-new-privileges \
  ghcr.io/gopoli/gopoli-api:latest
```

Si la base corre en otro contenedor, ambos deben compartir red y `SPRING_DATASOURCE_URL` debe apuntar al nombre del contenedor (por ejemplo `jdbc:postgresql://gopoli-db:5432/gopoli`). Para levantar el stack completo usa los entornos de [GoPoli/.github](https://github.com/GoPoli/.github/tree/main/docker).

### 5. Publicación en GitHub Container Registry

La publicación es automática: cada push a `main` construye la imagen y la publica en `ghcr.io/gopoli/gopoli-api` con las etiquetas `latest` y `sha-<commit>`, junto con su SBOM y la atestación de procedencia. Un tag `vX.Y.Z` publica además `X.Y.Z` y `X.Y`.

### 6. Despliegue

- Servidor propio con Nginx y HTTPS: [GoPoli/.github · production](https://github.com/GoPoli/.github/tree/main/docker/production).
- Railway, con base en Neon o en Railway: [docs/DEPLOY_RAILWAY.md](docs/DEPLOY_RAILWAY.md).

## Configuración

### Variables de Entorno

| Variable | Descripción | Valor por defecto |
| --- | --- | --- |
| `PORT` | Puerto HTTP de la API | `8080` |
| `APP_TIMEZONE` | Zona horaria de fechas y horas de negocio | `America/Bogota` |
| `LOG_LEVEL` | Nivel de log raíz | `INFO` |
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/gopoli` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `gopoli` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `gopoli` |
| `DB_POOL_SIZE` | Conexiones máximas del pool Hikari | `10` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia de esquema; la base la construye GoPoli-DB | `validate` |
| `SPRING_JPA_SHOW_SQL` / `SPRING_JPA_FORMAT_SQL` | SQL en el log | `false` |
| `GOPOLI_JWT_SECRET` | Secreto HMAC para firmar tokens (obligatorio, mínimo 32 caracteres) | — |
| `GOPOLI_JWT_EXPIRATION_HOURS` | Vigencia del token en horas | `168` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos, separados por comas | `http://localhost:3000` |
| `JAVA_TOOL_OPTIONS` | Opciones de la JVM dentro del contenedor | `-XX:MaxRAMPercentage=75 -XX:+ExitOnOutOfMemoryError` |

> [!WARNING]
> Genera un `GOPOLI_JWT_SECRET` propio por entorno, por ejemplo con `openssl rand -base64 48`. `CORS_ALLOWED_ORIGINS` debe contener exactamente la URL pública de la PWA; cualquier otro origen recibe `403`.

### Comportamiento en producción

- Apagado ordenado (`server.shutdown=graceful`) con 20 segundos para terminar peticiones en curso.
- Compresión de respuestas JSON y errores sin trazas ni mensajes internos.
- `open-in-view` desactivado y escrituras en lote de Hibernate.

### Neon (PostgreSQL en la nube)

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxxx-pooler.region.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=<password>
```

El esquema se crea en Neon con los scripts de GoPoli-DB. Guía completa en [GoPoli-DB · Neon](https://github.com/GoPoli/GoPoli-DB/blob/main/docs/DATABASE_NEON.md).

## Testing

```bash
./mvnw test
./mvnw test -Dtest=JwtServiceTest
```

La suite cubre `JwtService`, `PasswordService`, `GoPoliConstants`, `ProfilePolicy`, `TripPolicy`, `VehicleValidator` y la normalización de días de `RecurringRouteController`.

## CI/CD

| Workflow | Disparador | Qué hace |
| --- | --- | --- |
| `maven.yml` | Push y PR a `main` | Compila y ejecuta las pruebas con Maven |
| `codeql.yml` | Push, PR y semanal | Análisis estático de seguridad (Java y Actions) |
| `dependency-review.yml` | PR a `main` | Bloquea dependencias con vulnerabilidades conocidas |
| `packaging.yml` | Push a `main`, tags `v*.*.*`, manual | Construye y publica la imagen en GHCR con SBOM y provenance |
| `stale.yml` | Diario | Marca y cierra issues y PRs inactivos |

Dependabot revisa semanalmente Maven, la imagen base de Docker y las versiones de las Actions.

## Contribución

Lee la [guía de contribución](https://github.com/GoPoli/.github/blob/main/CONTRIBUTING.md) de la organización.

## Licencia

Este proyecto está bajo la licencia MIT. Consulta el archivo [LICENSE](LICENSE) para más detalles.

## Política de Seguridad

Consulta la [política de seguridad](https://github.com/GoPoli/.github/blob/main/SECURITY.md) para reportar vulnerabilidades.

## Autores

- Michael Daniel ([MaicolD0930](https://github.com/MaicolD0930))
- Jorge Martinez ([GeorgeAMS](https://github.com/GeorgeAMS))
- Marian Lasney
- Sebastián López O ([sebastianlopezo](https://github.com/sebastianlopezo))
