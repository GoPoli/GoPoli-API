# GoPoli-API

## Estado del Proyecto

[![Java CI with Maven](https://github.com/GoPoli/GoPoli-API/actions/workflows/maven.yml/badge.svg)](https://github.com/GoPoli/GoPoli-API/actions/workflows/maven.yml)
[![CodeQL Advanced](https://github.com/GoPoli/GoPoli-API/actions/workflows/codeql.yml/badge.svg)](https://github.com/GoPoli/GoPoli-API/actions/workflows/codeql.yml)
[![Publish Package to GHCR](https://github.com/GoPoli/GoPoli-API/actions/workflows/packaging.yml/badge.svg)](https://github.com/GoPoli/GoPoli-API/actions/workflows/packaging.yml)
[![Dependabot Updates](https://github.com/GoPoli/GoPoli-API/actions/workflows/dependabot/dependabot-updates/badge.svg)](https://github.com/GoPoli/GoPoli-API/actions/workflows/dependabot/dependabot-updates)

## Descripción

GoPoli-API es la API REST de **GoPoli**, la plataforma de viajes compartidos entre estudiantes del Politécnico Colombiano Jaime Isaza Cadavid. Está desarrollada en **Java 17** con **Spring Boot 4**, persiste en **PostgreSQL** mediante **Spring Data JPA** y autentica con **JWT**. Gestiona cuentas, catálogo académico, viajes compartidos, chat de grupo, agenda de rutas habituales y registro de conductores.

La API es consumida por la PWA [GoPoli-Web](https://github.com/GoPoli/GoPoli-Web) y usa la base de datos de [GoPoli-DB](https://github.com/GoPoli/GoPoli-DB).

## Tecnologías Principales

- **Java 17** - Lenguaje de programación
- **Spring Boot 4.0** - Framework principal (Web MVC)
- **Spring Data JPA / Hibernate** - Persistencia de datos
- **PostgreSQL 16** - Base de datos relacional
- **JWT (jjwt 0.12.6)** - Tokens de autenticación
- **Spring Security Crypto (BCrypt)** - Hash de contraseñas
- **Lombok** - Reducción de código boilerplate
- **Maven** - Gestión de dependencias y build
- **Docker** - Contenedorización
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
│       ├── packaging.yml               # Publicación de la imagen en GHCR
│       └── stale.yml                   # Limpieza de issues y PRs inactivos
│
├── docs/
│   └── DEPLOY_RAILWAY.md               # Guía de despliegue en Railway
│
├── src/
│   ├── main/
│   │   ├── java/com/proyect/gopoli/
│   │   │   ├── config/                 # UbicacionCoordenadasSeeder
│   │   │   ├── controller/             # Auth, Usuario, Catalogo, Servicio, Mensaje, Agenda
│   │   │   ├── dto/                    # LoginResponse, UsuarioDto, VehiculoDto
│   │   │   ├── model/                  # Entidades JPA y constantes de negocio
│   │   │   ├── repository/             # Repositorios Spring Data JPA
│   │   │   ├── security/               # JwtService, PasswordService
│   │   │   ├── util/                   # PerfilPolicy, ServicioPolicy, VehicleValidator
│   │   │   └── GoPoliApplication.java  # Clase principal
│   │   └── resources/
│   │       └── application.properties  # Configuración parametrizada por entorno
│   └── test/java/com/Proyect/GoPoli/   # Pruebas unitarias
│
├── .dockerignore
├── .env.example                        # Plantilla de variables de entorno
├── Dockerfile                          # Imagen multi-etapa (Maven → JRE 17)
├── LICENSE
├── mvnw / mvnw.cmd                     # Maven Wrapper
└── pom.xml
```

## Descripción de Módulos

### Controller

| Controlador | Responsabilidad |
| --- | --- |
| `AuthController` | Registro con correo institucional `@elpoli.edu.co` e inicio de sesión con emisión de JWT |
| `UsuarioController` | Perfil del usuario autenticado, foto, registro de conductor, historial, inhabilitar y eliminar cuenta |
| `CatalogoController` | Catálogos públicos de carreras y ubicaciones |
| `ServicioController` | Ciclo de vida de los viajes: crear, unirse, salir, iniciar, finalizar, cancelar y consultas |
| `MensajeController` | Chat del grupo de cada viaje |
| `AgendaController` | Rutas habituales del usuario |

### Model

Entidades JPA: `Usuario`, `Vehiculo`, `Carrera`, `TipoUsuario`, `Ubicacion`, `Servicio`, `ServicioUsuario`, `Mensaje` y `RutaHabitual`. Las constantes de negocio viven en `GoPoliConstants`:

| Concepto | Valores |
| --- | --- |
| Tipo de usuario | Pasajero `1`, Conductor `2` |
| Tipo de viaje | Grupo de pasajeros `1`, Grupo de conductor `3` |
| Estado del viaje | Activo `1`, Cancelado `2`, Finalizado `3`, En curso `4` |
| Rol en el grupo | `Creador`, `Miembro` |

### Security

- `JwtService`: firma y valida tokens HMAC con el secreto `GOPOLI_JWT_SECRET`. El `subject` del token es el id del usuario.
- `PasswordService`: hash BCrypt; acepta contraseñas heredadas en texto plano para no bloquear cuentas previas al hash.

### Util

Políticas de validación de negocio: formato de perfil (`PerfilPolicy`), transiciones y creación de viajes (`ServicioPolicy`) y datos del vehículo (`VehicleValidator`).

### Config

`UbicacionCoordenadasSeeder` sincroniza al arrancar las coordenadas de las ubicaciones conocidas (estaciones del metro y salidas del campus).

## Mapa de la API

Base local: `http://localhost:8080`. Todas las rutas, salvo las públicas, requieren el encabezado `Authorization: Bearer <token>`.

| Método | Ruta | Acceso |
| --- | --- | --- |
| `POST` | `/register` | Público |
| `POST` | `/login` | Público |
| `GET` | `/carreras` | Público |
| `GET` | `/ubicaciones` | Público |
| `GET` `PUT` `DELETE` | `/usuario/me` | JWT |
| `PUT` | `/usuario/me/foto` | JWT |
| `POST` | `/usuario/me/register-driver` · `/usuario/me/unregister-driver` | JWT |
| `GET` | `/usuario/me/historial-viajes` | JWT |
| `POST` | `/usuario/me/inhabilitar` | JWT |
| `POST` | `/servicio/crear` · `/servicio/unirse` | JWT |
| `GET` | `/servicios/activos` | JWT |
| `GET` | `/servicio/{id}` · `/servicio/{id}/miembros` | JWT |
| `PUT` | `/servicio/iniciar/{id}` · `/servicio/finalizar/{id}` · `/servicio/cancelar/{id}` | JWT |
| `DELETE` | `/servicio/salir/{idServicio}/{idUsuario}` | JWT |
| `GET` | `/servicio/usuario/activo/{id}` · `/miembro/{id}` · `/encurso/{id}` | JWT |
| `GET` `POST` | `/servicio/{id}/mensajes` | JWT |
| `GET` | `/agenda/rutas/usuario/{idUsuario}` | JWT (solo el dueño) |
| `POST` | `/agenda/rutas` | JWT |
| `PUT` `DELETE` | `/agenda/rutas/{idRuta}` | JWT (solo el dueño) |

## Guía de Instalación

### Requisitos Previos

- Java 17 o superior
- Maven 3.9+ (o el Maven Wrapper incluido)
- PostgreSQL 16 (recomendado: la imagen de [GoPoli-DB](https://github.com/GoPoli/GoPoli-DB))
- Docker (opcional, para contenedorización)

### 1. Levantar la Base de Datos

```bash
docker run -d --name gopoli-db -p 5432:5432 \
  -e POSTGRES_DB=gopoli \
  -e POSTGRES_USER=gopoli \
  -e POSTGRES_PASSWORD=gopoli \
  ghcr.io/gopoli/gopoli-db:latest
```

La imagen crea el esquema y carga los catálogos y un usuario demo en el primer arranque. Detalles en [GoPoli-DB](https://github.com/GoPoli/GoPoli-DB).

### 2. Ejecución Local

```bash
git clone https://github.com/GoPoli/GoPoli-API.git
cd GoPoli-API
./mvnw spring-boot:run
```

En Windows usa `mvnw.cmd spring-boot:run`. Sin variables de entorno, la API se conecta a `localhost:5432/gopoli` con usuario y contraseña `gopoli`, que coinciden con la base del paso anterior.

Comprobación:

```bash
curl http://localhost:8080/ubicaciones
```

### 3. Construcción de la Imagen Docker

El `Dockerfile` es multi-etapa: compila con Maven dentro del contenedor y genera una imagen final con JRE 17 que se ejecuta con un usuario sin privilegios. No hace falta tener Java instalado.

```bash
docker build --platform linux/amd64 -t ghcr.io/gopoli/gopoli-api:latest .
```

### 4. Ejecución con Docker

```bash
cp .env.example .env
docker run -d --name gopoli-api -p 8080:8080 --env-file .env ghcr.io/gopoli/gopoli-api:latest
```

Si la base corre en otro contenedor, ambos deben compartir red y `SPRING_DATASOURCE_URL` debe apuntar al nombre del contenedor (por ejemplo `jdbc:postgresql://gopoli-db:5432/gopoli`). Para levantar el stack completo usa los entornos de [GoPoli/.github](https://github.com/GoPoli/.github/tree/main/docker).

### 5. Publicación en GitHub Container Registry

La publicación es automática: cada push a `main` construye la imagen y la publica en `ghcr.io/gopoli/gopoli-api` con las etiquetas `latest` y `sha-<commit>`. Un tag `vX.Y.Z` publica además `X.Y.Z` y `X.Y`.

Publicación manual (requiere un token con `write:packages`):

```bash
echo $GHCR_TOKEN | docker login ghcr.io -u <usuario> --password-stdin
docker push ghcr.io/gopoli/gopoli-api:latest
```

### 6. Despliegue en la Nube

Guía para Railway, con base en Neon o en Railway: [docs/DEPLOY_RAILWAY.md](docs/DEPLOY_RAILWAY.md).

## Configuración

### Variables de Entorno

Copia `.env.example` a `.env` y ajusta los valores. Spring Boot no lee `.env` por sí mismo: úsalo con `docker run --env-file`, con Docker Compose o en la configuración de ejecución del IDE.

| Variable | Descripción | Valor por defecto |
| --- | --- | --- |
| `PORT` | Puerto HTTP de la API | `8080` |
| `SPRING_DATASOURCE_URL` | URL JDBC de PostgreSQL | `jdbc:postgresql://localhost:5432/gopoli` |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos | `gopoli` |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos | `gopoli` |
| `SPRING_JPA_HIBERNATE_DDL_AUTO` | Estrategia de esquema de Hibernate | `update` |
| `SPRING_JPA_SHOW_SQL` | Muestra el SQL generado en el log | `true` |
| `SPRING_JPA_FORMAT_SQL` | Formatea el SQL del log | `true` |
| `GOPOLI_JWT_SECRET` | Secreto HMAC para firmar tokens (mínimo 32 caracteres) | Valor de desarrollo |
| `GOPOLI_JWT_EXPIRATION_HOURS` | Vigencia del token en horas | `168` |
| `JAVA_TOOL_OPTIONS` | Opciones de la JVM dentro del contenedor | `-XX:MaxRAMPercentage=75` |

> [!WARNING]
> El secreto JWT por defecto solo sirve para desarrollo. En cualquier entorno compartido define `GOPOLI_JWT_SECRET`, por ejemplo con `openssl rand -base64 48`.

### Neon (PostgreSQL en la nube)

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://ep-xxxx-pooler.region.aws.neon.tech/neondb?sslmode=require
SPRING_DATASOURCE_USERNAME=neondb_owner
SPRING_DATASOURCE_PASSWORD=<password>
```

Guía completa en [GoPoli-DB · Neon](https://github.com/GoPoli/GoPoli-DB/blob/main/docs/DATABASE_NEON.md).

## Testing

```bash
./mvnw test
./mvnw test -Dtest=JwtServiceTest
```

La suite cubre `JwtService`, `PasswordService`, `GoPoliConstants`, `PerfilPolicy`, `ServicioPolicy` y `VehicleValidator`. La prueba de contexto completo (`GoPoliApplicationTests`) está deshabilitada porque necesita una base PostgreSQL disponible.

## CI/CD

| Workflow | Disparador | Qué hace |
| --- | --- | --- |
| `maven.yml` | Push y PR a `main` | Compila y ejecuta las pruebas con Maven |
| `codeql.yml` | Push, PR y semanal | Análisis estático de seguridad (Java y Actions) |
| `dependency-review.yml` | PR a `main` | Bloquea dependencias con vulnerabilidades conocidas |
| `packaging.yml` | Push a `main`, tags `v*.*.*`, manual | Construye y publica la imagen en GHCR |
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
