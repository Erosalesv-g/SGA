# Sistema de Gestión Académica (SGA)

Sistema de gestión académica para la Unidad Educativa Fiscal "Durán", desarrollado con Spring Boot (backend) y React + TypeScript (frontend).

## Tecnologías

- **Backend:** Java 21, Spring Boot 3.3, Spring Security, JWT, PostgreSQL, Redis, Flyway
- **Frontend:** React, TypeScript, Vite

---

## Requisitos previos

Antes de empezar, instala lo siguiente en tu computadora:

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

Esto crea automáticamente los contenedores `sga_postgres` y `sga_redis`.

### 3. Levantar el backend

```bash
cd unemi
.\mvnw spring-boot:run
```

> En Mac/Linux usa `./mvnw spring-boot:run`

Espera hasta ver: `Started UnemiApplication in X seconds`. Las tablas se crean automáticamente con Flyway.

### 4. Crear el usuario administrador

Una vez el backend esté corriendo, abre pgAdmin o cualquier cliente de PostgreSQL y ejecuta en la base de datos `sga_db`:

```sql
INSERT INTO usuarios (id, nombre, email, password_hash, rol, activo, intentos_fallidos)
VALUES (
    gen_random_uuid(),
    'Administrador',
    'admin@sga.edu.ec',
    '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBpwTpyAgMkEGe',
    'RECTOR',
    true,
    0
);
```

### 5. Levantar el frontend

En una terminal separada (no cierres la del backend):

```bash
cd frontend
npm install
npm run dev
```

### 6. Abrir la aplicación

Ve a [http://localhost:5173](http://localhost:5173) en tu navegador.

---

## Credenciales de prueba

| Campo    | Valor              |
|----------|--------------------|
| Usuario  | admin@sga.edu.ec   |
| Contraseña | password         |

---

## Estructura del proyecto

SGA/

├── frontend/          # Aplicación React + TypeScript (Vite)

│   ├── src/

│   │   ├── api/       # Cliente HTTP (axios)

│   │   ├── assets/    # Imágenes y recursos estáticos

│   │   ├── pages/     # Páginas (Login, Dashboard, etc.)

│   │   └── types/     # Tipos TypeScript

│   └── package.json

├── unemi/             # Backend Spring Boot

│   ├── src/main/java/com/sga/unemi/

│   │   ├── controller/    # Endpoints REST

│   │   ├── model/         # Entidades JPA

│   │   ├── repository/    # Repositorios

│   │   ├── service/       # Lógica de negocio

│   │   └── security/      # JWT y Spring Security

│   └── src/main/resources/

│       ├── db/migration/  # Migraciones Flyway

│       └── application.properties

├── docker-compose.yml # PostgreSQL + Redis

└── README.md

---

## Estado actual del proyecto

### ✅ Completado
- Autenticación con JWT y Redis (login, logout, bloqueo por intentos fallidos)
- Módulo de usuarios (CRUD)
- Módulo de estudiantes
- Módulo de docentes
- Módulo de calificaciones
- Módulo de asistencia
- Módulo de horarios
- Módulo de comunicados
- Módulo de reportes
- Pantalla de Login (frontend)

### 🚧 En desarrollo
- Dashboard con sidebar y menús por rol
- Pantallas CRUD para cada módulo
- Vista responsive para móviles
- Configuración PWA

---

## Solución de problemas comunes

### Puerto 8080 ocupado
```bash
netstat -ano | findstr :8080
taskkill /PID <numero_PID> /F
```
Luego vuelve a correr `.\mvnw spring-boot:run`.

### Contenedores de Docker caídos
```bash
docker start sga_postgres sga_redis
```

### Token expirado / error 401
El token dura 15 minutos. Si pasa este tiempo, vuelve a iniciar sesión en la aplicación.

### Flyway: error de migración
```sql
DELETE FROM flyway_schema_history WHERE version = '2';
```
Luego reinicia el backend.

---

## Detener el proyecto

Para detener los contenedores de Docker:
```bash
docker compose down
```

Para detener el backend y frontend, presiona `Ctrl + C` en cada terminal.