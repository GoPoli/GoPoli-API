# Deploy GoPoli en Railway

## 1) Backend + PostgreSQL

**Opcion A — BD en Neon (recomendado para el equipo):** crea el proyecto en Neon y sigue [`DATABASE_NEON.md`](DATABASE_NEON.md). En Railway solo despliegas el backend con las mismas variables `SPRING_DATASOURCE_*` (URL con pooler y `?sslmode=require`).

**Opcion B — PostgreSQL en Railway:**

1. Sube repo a GitHub (rama actual).
2. En Railway: **New Project** -> **Deploy from GitHub repo**.
3. Crea un servicio PostgreSQL en el mismo proyecto.
4. En el servicio backend, configura:
   - Root Directory: `backend`
   - Variables:
     - `SPRING_DATASOURCE_URL`
     - `SPRING_DATASOURCE_USERNAME`
     - `SPRING_DATASOURCE_PASSWORD`
   - Railway tambien inyecta `PORT`; Spring Boot lo lee desde `server.port=${PORT:8080}`.

## 2) URL publica del backend

Cuando el servicio quede en estado **Healthy**, copia la URL publica:

- Ejemplo: `https://gopoli-backend.up.railway.app`

Prueba:

- `GET https://gopoli-backend.up.railway.app/ubicaciones`

## 3) PWA Next.js apuntando a Railway

La PWA vive en `web/` (host Node separado). Configura la URL publica del API:

```env
NEXT_PUBLIC_API_URL=https://gopoli-backend.up.railway.app
```

En local: `web/.env.local`. En el host de la PWA: variables de entorno del proveedor. Detalle: [`web/DEPLOYMENT.md`](web/DEPLOYMENT.md).

Ejemplo de arranque local contra Railway:

```bash
cd web
npm install
npm run dev
```

## 4) Migrar datos de BD local a Railway (opcional)

Exportar local:

```bash
pg_dump -h localhost -U postgres -d gopoli -F c -f gopoli.dump
```

Importar a Railway (usa host/port/user/db del servicio PostgreSQL Railway):

```bash
pg_restore --no-owner --no-privileges -h <HOST> -p <PORT> -U <USER> -d <DB> gopoli.dump
```

## 5) Checklist final

- Backend en Railway responde endpoints.
- La PWA inicia sesion usando `NEXT_PUBLIC_API_URL` hacia Railway.
- CORS abierto para desarrollo (`@CrossOrigin("*")`) o restringido en produccion.
- Secretos de BD / JWT solo en el backend (no en el cliente).
