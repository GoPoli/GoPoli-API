# Despliegue de GoPoli-API en Railway

Esta guía describe cómo publicar la API en [Railway](https://railway.app) usando el `Dockerfile` del repositorio o la imagen ya publicada en GitHub Container Registry.

---

## 1. Base de datos

Elige una de estas opciones antes de crear el servicio de la API:

| Opción | Cuándo usarla | Guía |
| --- | --- | --- |
| **Neon** | Base compartida por el equipo, siempre disponible | [GoPoli-DB · Neon](https://github.com/GoPoli/GoPoli-DB/blob/main/docs/DATABASE_NEON.md) |
| **PostgreSQL en Railway** | Todo el despliegue dentro del mismo proyecto de Railway | Crear un servicio **PostgreSQL** en el proyecto |

En ambos casos necesitas tres valores: `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`.

> [!IMPORTANT]
> La URL debe estar en formato JDBC (`jdbc:postgresql://HOST:PUERTO/BASE`). Con Neon agrega `?sslmode=require` y usa el host con **pooler**.

---

## 2. Servicio de la API

### Opción A: desde el repositorio (Dockerfile)

1. En Railway: **New Project** → **Deploy from GitHub repo** → `GoPoli/GoPoli-API`.
2. Railway detecta el `Dockerfile` de la raíz y construye la imagen multi-etapa.
3. Cada push a `main` dispara un nuevo despliegue.

### Opción B: desde la imagen de GHCR

1. En Railway: **New** → **Docker Image**.
2. Imagen: `ghcr.io/gopoli/gopoli-api:latest`.
3. Si el paquete es privado, configura las credenciales del registro con un token de GitHub con permiso `read:packages`.

---

## 3. Variables de entorno

| Variable | Valor |
| --- | --- |
| `SPRING_DATASOURCE_URL` | URL JDBC de la base de datos |
| `SPRING_DATASOURCE_USERNAME` | Usuario de la base de datos |
| `SPRING_DATASOURCE_PASSWORD` | Contraseña de la base de datos |
| `GOPOLI_JWT_SECRET` | Secreto de al menos 32 caracteres (`openssl rand -base64 48`) |
| `SPRING_JPA_SHOW_SQL` | `false` en producción |
| `SPRING_JPA_FORMAT_SQL` | `false` en producción |

Railway inyecta `PORT` automáticamente y la API lo respeta (`server.port=${PORT:8080}`).

---

## 4. Verificación

Cuando el servicio quede en estado **Healthy**, copia la URL pública (por ejemplo `https://gopoli-api.up.railway.app`) y prueba:

```bash
curl https://gopoli-api.up.railway.app/ubicaciones
```

Luego apunta la PWA a esa URL con `NEXT_PUBLIC_API_URL` (ver [GoPoli-Web](https://github.com/GoPoli/GoPoli-Web#variables-de-entorno)).

---

## 5. Migrar datos locales

Para llevar una base local a Railway o Neon, usa el procedimiento de `pg_dump` / `pg_restore` documentado en [GoPoli-DB](https://github.com/GoPoli/GoPoli-DB/blob/main/docs/DATABASE_NEON.md#3-migrar-una-base-local-a-neon).

---

## 6. Checklist final

- [ ] La API responde `GET /ubicaciones` y `GET /carreras`.
- [ ] `GOPOLI_JWT_SECRET` es propio del entorno y no el valor de desarrollo.
- [ ] La PWA inicia sesión contra la URL pública de la API.
- [ ] Los secretos de base de datos y JWT solo existen en el servicio de la API, nunca en el cliente.
