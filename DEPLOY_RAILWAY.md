# Deploy GoPoli en Railway

## 1) Backend + PostgreSQL

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

## 3) Frontend Flutter apuntando a Railway

No necesitas editar codigo. Usa `--dart-define`:

```bash
flutter run --dart-define=API_URL=https://gopoli-backend.up.railway.app
```

Para build release:

```bash
flutter build apk --release --dart-define=API_URL=https://gopoli-backend.up.railway.app
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
- App movil inicia sesion usando `API_URL` de Railway.
- CORS abierto para desarrollo (`@CrossOrigin("*")`) o restringido en produccion.
- Claves secretas no subidas a git (`google_maps_config.dart` local + `.gitignore`).
