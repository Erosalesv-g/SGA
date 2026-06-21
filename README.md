# Sistema de Gestión Académica (SGA)

Sistema de gestión académica para la Unidad Educativa Fiscal "Durán", desarrollado con Spring Boot (backend) y React + TypeScript (frontend), configurado como Progressive Web App (PWA) con soporte offline de solo lectura.

## Tecnologías

- **Backend:** Java 21, Spring Boot 3.3, Spring Security, JWT, PostgreSQL, Redis, Flyway
- **Frontend:** React, TypeScript, Vite, vite-plugin-pwa

---

## Requisitos previos

1. **Java 21** → [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=21)
2. **Node.js** (versión 18 o superior) → [nodejs.org](https://nodejs.org)
3. **Docker Desktop** → [docker.com](https://www.docker.com/products/docker-desktop/)
4. **Git** → [git-scm.com](https://git-scm.com/downloads)

---

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/Erosalesv-g/SGA.git
cd SGA
```

### 2. Levantar la base de datos (PostgreSQL + Redis)

```bash
docker compose up -d
```

### 3. Levantar el backend

```bash
cd unemi
.\mvnw spring-boot:run
```

> En Mac/Linux usa `./mvnw spring-boot:run`

Las migraciones de Flyway se aplican automáticamente al iniciar.

### 4. Levantar el frontend

En una terminal separada:

```bash
cd frontend
npm install
npm run dev
```

### 5. Abrir la aplicación

Ve a [http://localhost:5173](http://localhost:5173).

### 6. Crear el primer usuario administrador

La base de datos empieza vacía — no hay ningún usuario para iniciar sesión. Crea el primero manualmente con pgAdmin (o cualquier cliente PostgreSQL), conectado a `sga_db`:

```sql
INSERT INTO usuarios (id, nombre, email, password_hash, rol, activo, intentos_fallidos)
VALUES (
    gen_random_uuid(),
    'Administrador',
    'admin@sga.edu.ec',
    '$2a$10$bMvpQr51nfLksO2M1a5zsOfroun8HfAxYfJt0ZkFl3ywuME0VEW1S',
    'RECTOR',
    true,
    0
);
```

Esto crea el usuario `admin@sga.edu.ec` con contraseña `password`. Una vez dentro, puedes crear el resto de usuarios (docentes, estudiantes, representantes, etc.) desde la pantalla de **Usuarios** del sistema.

---

## Credenciales de prueba

| Campo    | Valor              |
|----------|--------------------|
| Usuario  | admin@sga.edu.ec   |
| Contraseña | password         |
| Rol | RECTOR |

---

## Roles del sistema

El sistema diferencia 6 tipos de usuario, cada uno con su propio menú y datos filtrados:

| Rol | Acceso |
|-----|--------|
| **RECTOR** | Acceso total: usuarios, estudiantes, docentes, materias, calificaciones, asistencia, horarios, comunicados, reportes |
| **INSPECTOR** | Estudiantes, asistencia (gestión), comunicados |
| **DOCENTE** | Sus propias calificaciones registradas, su horario, asistencia de sus materias, comunicados |
| **ESTUDIANTE** | Solo sus propias calificaciones, asistencia, horario (por nivel), comunicados dirigidos a estudiantes, su boletín/reporte |
| **REPRESENTANTE** | Calificaciones, asistencia y reportes del/los estudiante(s) que representa (vinculado vía `representante_id`) |
| **ORIENTADOR (DECE)** | Estudiantes, asistencia, comunicados, reportes |

---

## Estructura del proyecto
SGA/

├── frontend/

│   ├── src/

│   │   ├── api/           # Cliente HTTP (axios) con bloqueo offline

│   │   ├── assets/

│   │   ├── components/    # ProtectedRoute, etc.

│   │   ├── pages/         # Login, Dashboard, Inicio, Estudiantes, Docentes,

│   │   │                  # Materias, Calificaciones, Asistencias, Horarios,

│   │   │                  # Comunicados, Reportes, Usuarios

│   │   └── types/

│   ├── public/             # Íconos PWA

│   └── vite.config.ts      # Configuración de PWA y caché offline

├── unemi/                  # Backend Spring Boot

│   ├── src/main/java/com/sga/unemi/

│   │   ├── controller/

│   │   ├── dto/

│   │   ├── model/

│   │   ├── repository/

│   │   ├── service/

│   │   └── security/

│   └── src/main/resources/

│       ├── db/migration/   # V1 a V4 (incluye representante_id y nivel en materias)

│       └── application.properties

├── docker-compose.yml

└── README.md

---

## Funcionalidades implementadas

### Backend
- Autenticación JWT con bloqueo por intentos fallidos (Redis)
- CRUD completo: Usuarios, Estudiantes, Docentes, Materias, Calificaciones, Asistencia, Horarios, Comunicados
- Reportes: boletín de calificaciones y resumen de asistencia por estudiante
- Validación de rango de notas (0-10)
- Validación de conflicto de horarios (mismo docente, mismo día/hora)
- Relación Representante ↔ Estudiante

### Frontend
- Login responsive con diseño institucional
- Dashboard con sidebar (menú hamburguesa en celular) y menú diferenciado por rol
- Datos filtrados según el rol logueado (no solo el menú, también el contenido)
- Pantalla de Inicio con estadísticas (RECTOR) o comunicados recientes (otros roles)
- Botón de reactivar para estudiantes/docentes desactivados
- PWA instalable con modo offline (cachea la última información cargada; bloquea creación/edición sin conexión)

---

## Probar el modo offline (PWA)

El servidor de desarrollo (`npm run dev`) no soporta bien el caché offline. Para probarlo de verdad:

```bash
cd frontend
npm run build
npm run preview
```

Abre la URL que te dé (normalmente `http://localhost:4173`), inicia sesión, navega por la app, y luego simula estar sin conexión (DevTools → Network → "Offline") para confirmar que la última información cargada sigue visible.

---

## Solución de problemas comunes

### Puerto 8080 ocupado
```bash
netstat -ano | findstr :8080
taskkill /PID <numero_PID> /F
```

### Contenedores de Docker caídos
```bash
docker start sga_postgres sga_redis
```

### Cuenta bloqueada tras intentos fallidos
El bloqueo se guarda en Redis (no en la base de datos). Para desbloquear manualmente:
```bash
docker exec -it sga_redis redis-cli FLUSHALL
```

### Token expirado / error 401 o 403 inesperado
El token dura 15 minutos. Si pasa este tiempo, vuelve a iniciar sesión.

---

## Detener el proyecto

```bash
docker compose down
```

Para detener el backend y frontend, `Ctrl + C` en cada terminal.a 