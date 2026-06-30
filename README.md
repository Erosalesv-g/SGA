# Sistema de Gestión Académica (SGA)

Sistema de gestión académica para la Unidad Educativa Fiscal "Durán", desarrollado con Spring Boot (backend) y React + TypeScript (frontend), configurado como Progressive Web App (PWA) con soporte offline de solo lectura.

## Tecnologías

- **Backend:** Java 21, Spring Boot 3.3, Spring Security, JWT (RS256), PostgreSQL, Redis, RabbitMQ, MinIO, Flyway, Resilience4j, OpenPDF, Logstash Logback Encoder
- **Frontend:** React, TypeScript, Vite, vite-plugin-pwa

---

## Requisitos previos

1. **Java 21** → [Adoptium Temurin](https://adoptium.net/temurin/releases/?version=21)
2. **Node.js** (versión 18 o superior) → [nodejs.org](https://nodejs.org)
3. **Docker Desktop** → [docker.com](https://www.docker.com/products/docker-desktop/)
4. **Git** → [git-scm.com](https://git-scm.com/downloads)

> **Importante (Windows):** si tienes PostgreSQL instalado de forma nativa en tu PC (no en Docker), asegúrate de que el servicio de Windows esté detenido o configurado en "Manual" antes de levantar los contenedores. Si ambos intentan usar el puerto 5432 a la vez, el backend puede terminar conectándose al Postgres equivocado sin avisar (ver sección de Solución de problemas).

---

## Instalación

### 1. Clonar el repositorio

```bash
git clone https://github.com/Erosalesv-g/SGA.git
cd SGA
```

### 2. Levantar la infraestructura (PostgreSQL, Redis, RabbitMQ, MinIO)

```bash
docker compose up -d
```

Esto levanta 4 contenedores:

| Servicio | Puerto | Uso |
|---|---|---|
| PostgreSQL | 5432 | Base de datos principal |
| Redis | 6379 | Sesiones, bloqueo por intentos fallidos, caché de Materias/Horarios |
| RabbitMQ | 5672 (AMQP), 15672 (consola web) | Envío asíncrono de comunicados y generación masiva de boletines |
| MinIO | 9000 (API), 9001 (consola web) | Almacenamiento de recursos pedagógicos y backups cifrados |

Confirma que los 4 estén corriendo con `docker ps`.

### 3. Levantar el backend

```bash
cd unemi
.\mvnw spring-boot:run
```

> En Mac/Linux usa `./mvnw spring-boot:run`

Las 16 migraciones de Flyway se aplican automáticamente al iniciar.

> **Nota sobre JWT:** el par de claves RS256 se genera en memoria al iniciar el backend (ver `JwtUtil`), no se configura una clave secreta en `application.properties`. Esto implica que los tokens emitidos antes de un reinicio del backend dejan de ser válidos después de reiniciar.

### 4. Levantar el frontend

En una terminal separada:

```bash
cd frontend
npm install
npm run dev
```

### 5. Abrir la aplicación

Ve a http://localhost:5173

### 6. Crear el primer usuario administrador

La base de datos empieza vacía — no hay ningún usuario para iniciar sesión. Crea el primero manualmente conectado al contenedor de Postgres:

```powershell
docker exec -it sga_postgres psql -U postgres -d sga_db -c 'INSERT INTO usuarios (id, nombre, email, password_hash, rol, activo, intentos_fallidos) VALUES (gen_random_uuid(), ''Administrador'', ''admin@sga.edu.ec'', ''$2b$10$BpH51d3iq9pWHdfLNtYcEuJfgZ/3to/ecTbaoO7ucfVD0nkzru15q'', ''RECTOR'', true, 0);'
```

> **Importante (PowerShell en Windows):** el comando usa comillas simples a propósito. El hash de la contraseña contiene signos `$` (`$2b$10$...`), y PowerShell los interpreta como variables dentro de comillas dobles, lo que corrompe el hash silenciosamente (nos pasó durante el desarrollo). Si necesitas escribir este tipo de comando desde cero, usa siempre comillas simples para el SQL completo, y duplica las comillas simples internas (`''`) para los valores de texto.

Esto crea el usuario `admin@sga.edu.ec` con contraseña `password`. Una vez dentro, puedes crear el resto de usuarios (docentes, estudiantes, representantes, etc.) desde la pantalla de Usuarios del sistema.

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
| RECTOR | Acceso total: usuarios, estudiantes, docentes, materias, calificaciones, asistencia, horarios, comunicados, reportes, boletines masivos |
| INSPECTOR | Estudiantes, asistencia (gestión), comunicados |
| DOCENTE | Sus propias calificaciones registradas, su horario, asistencia de sus materias, comunicados, subir recursos pedagógicos |
| ESTUDIANTE | Solo sus propias calificaciones, asistencia, horario (por nivel), comunicados dirigidos a estudiantes, su boletín/reporte |
| REPRESENTANTE | Calificaciones, asistencia y reportes del/los estudiante(s) que representa (vinculado vía representante_id) |
| ORIENTADOR (DECE) | Estudiantes, asistencia, comunicados, reportes |

---

## Patrones de diseño implementados

| Patrón | Dónde | Propósito |
|---|---|---|
| Strategy | strategy/PromedioStrategyFactory | Calcula el promedio de calificaciones con una fórmula distinta según el nivel del estudiante (promedio simple para Básica, ponderado 40/30/20/10 por tipo de evaluación para Bachillerato) |
| Observer | observer/CalificacionObserver | Notifica automáticamente al representante legal cuando se registra una nueva calificación |
| Singleton | Implícito vía el contenedor de Spring: todo @Bean/@Service/@Component es singleton por defecto. El pool de conexiones HikariCP (HikariDataSource) es una única instancia compartida en toda la aplicación |
| Circuit Breaker (Resilience4j) | service/ComunicadoEventPublisher, service/BoletinMasivoEventPublisher | Si RabbitMQ no responde, evita que la operación falle por completo: el registro se guarda igual, y el procesamiento queda pendiente para reintento manual |
| RBAC | Spring Security + filtrado por rol en cada servicio | Control de acceso basado en roles, verificado tanto en el endpoint como en los datos devueltos |

---

## Mensajería asíncrona (RabbitMQ)

### Comunicados
1. POST /api/comunicados guarda el comunicado y publica un evento a RabbitMQ — responde de inmediato.
2. ComunicadoConsumer procesa el evento en segundo plano, busca a todos los usuarios del rol destinatario, y les crea una notificación.
3. Si RabbitMQ no está disponible, el Circuit Breaker evita que esto tumbe la petición.

### Boletines masivos
1. POST /api/reportes/boletines-masivos/{nivel}?solicitadoPorId={id} crea un trabajo de seguimiento (TrabajoBoletinMasivo, estado PENDIENTE) y publica un evento a RabbitMQ — responde de inmediato.
2. BoletinMasivoConsumer genera el PDF de cada estudiante del nivel y lo sube a MinIO bajo boletines-masivos/{trabajoId}/{estudianteId}.pdf, actualizando el progreso en tiempo real.
3. GET /api/reportes/boletines-masivos/{trabajoId} consulta el estado del trabajo (PENDIENTE, PROCESANDO, COMPLETADO, COMPLETADO_CON_ERRORES, FALLIDO).

Consola de administración de RabbitMQ: http://localhost:15672 (usuario/clave: guest/guest).

---

## Seguridad

- Autenticación: JWT firmado con RS256 (par de claves asimétrico, generado en memoria al iniciar el backend), expiración de 15 minutos (access token) y 7 días (refresh token).
- Contraseñas: BCrypt con factor de costo 12.
- Bloqueo de cuenta: tras 5 intentos fallidos consecutivos, la cuenta se bloquea 15 minutos (vía Redis) y se notifica al usuario por correo (EmailService, best-effort: si SMTP no está configurado, el bloqueo se aplica igual, solo falla el envío del correo).
- Rate limiting: 100 peticiones/minuto por IP en /api/**, devuelve 429 al exceder.
- CORS: restringido al origen del frontend (http://localhost:5173).
- Headers de seguridad: X-Content-Type-Options, X-Frame-Options, X-XSS-Protection (por defecto de Spring Security) + Content-Security-Policy explícito.
- Manejo de errores: GlobalExceptionHandler centralizado (exception/) con jerarquía de excepciones de negocio (AcademicException, ResourceNotFoundException, ValidationException, UnauthorizedException), cada una traducida al código HTTP correcto (404, 400, 403) en vez de un 500 genérico.
- Auditoría: doble capa — a nivel de aplicación (auditoria_log, con detalle de usuario y operación) y a nivel de base de datos vía triggers de PostgreSQL (auditoria_triggers, en las tablas calificaciones y matriculas, capturando automáticamente tipo de operación y timestamp).
- Logs: estructurados en formato JSON (logs/sga.json), con un traceId único por petición HTTP (ver TraceIdFilter) para correlacionar todas las líneas de log de una misma solicitud.

---

## Estructura del proyecto

```
SGA/
├── frontend/
│   ├── src/
│   │   ├── api/
│   │   ├── assets/
│   │   ├── components/
│   │   ├── pages/
│   │   └── types/
│   ├── public/
│   └── vite.config.ts
├── unemi/
│   ├── src/main/java/com/sga/unemi/
│   │   ├── config/         # RabbitMQConfig, WebConfig, CacheConfig, TraceIdFilter
│   │   ├── consumer/       # ComunicadoConsumer, BoletinMasivoConsumer
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── exception/      # GlobalExceptionHandler + jerarquia de excepciones
│   │   ├── model/
│   │   ├── observer/
│   │   ├── repository/
│   │   ├── security/       # JwtUtil (RS256), RateLimitInterceptor
│   │   ├── service/        # EventPublishers (Circuit Breaker), EmailService
│   │   └── strategy/
│   └── src/main/resources/
│       ├── db/migration/   # V1 a V16
│       ├── application.properties
│       └── logback-spring.xml
├── scripts/
│   ├── backup-db.ps1                # Backup cifrado (AES-256) + sube a MinIO
│   ├── restore-db.ps1               # Descifra y restaura un backup
│   ├── datos_prueba_masivos.sql
│   └── particionamiento_demo.sql
├── backups/
├── docker-compose.yml
└── README.md
```

---

## Funcionalidades implementadas

### Backend
- Autenticación JWT (RS256) con bloqueo por intentos fallidos (Redis) y notificación por correo
- Rate limiting por IP (100 peticiones/minuto en /api/**, devuelve 429 al exceder)
- CRUD completo: Usuarios, Estudiantes, Docentes, Materias, Calificaciones, Asistencia, Horarios, Comunicados
- Comunicados masivos vía RabbitMQ con Circuit Breaker (Resilience4j)
- Generación masiva de boletines en PDF vía RabbitMQ, con seguimiento de progreso
- Reportes: boletín de calificaciones (JSON y PDF) y resumen de asistencia por estudiante
- Recursos pedagógicos almacenados en MinIO (subida restringida a rol DOCENTE)
- Validación de rango de notas (0-10)
- Validación de conflicto de horarios (mismo docente, mismo día/hora)
- Bloqueo optimista (@Version) en Matrículas
- Relación Representante ↔ Estudiante
- JOIN FETCH e índice compuesto en Calificaciones para evitar N+1
- Caché en Redis para Materias y Horarios
- Manejador global de excepciones con códigos HTTP correctos
- Triggers de auditoría automática en PostgreSQL
- Logs JSON estructurados con correlación por traceId

### Frontend
- Login responsive con diseño institucional
- Dashboard con sidebar (menú hamburguesa en celular) y menú diferenciado por rol
- Datos filtrados según el rol logueado
- Pantalla de Inicio con estadísticas (RECTOR) o comunicados recientes (otros roles)
- Botón de reactivar para estudiantes/docentes desactivados
- PWA instalable con modo offline

---

## Scripts de utilidad

### Backup y restauración de la base de datos (cifrados)

```powershell
.\scripts\backup-db.ps1
.\scripts\restore-db.ps1 -Archivo "backups\sga_backup_2026-06-29_12-41-45.sql.enc"
```

Los backups locales de más de 7 días se eliminan automáticamente al correr backup-db.ps1 de nuevo. La clave de cifrado se toma de la variable de entorno SGA_BACKUP_KEY; si no existe, usa una clave por defecto solo para desarrollo local.

### Generar datos de prueba a escala

```powershell
Get-Content scripts\datos_prueba_masivos.sql | docker exec -i sga_postgres psql -U postgres -d sga_db
```

### Demostración de particionamiento por año lectivo

```powershell
Get-Content scripts\particionamiento_demo.sql | docker exec -i sga_postgres psql -U postgres -d sga_db
```

Crea una tabla de demostración aislada, sin tocar las tablas reales de la aplicación.

---

## Probar el modo offline (PWA)

```bash
cd frontend
npm run build
npm run preview
```

Abre la URL que te dé (normalmente http://localhost:4173), inicia sesión, navega por la app, y simula estar sin conexión (DevTools → Network → "Offline").

---

## Solución de problemas comunes

### Puerto 8080 ocupado
```bash
netstat -ano | findstr :8080
taskkill /PID <numero_PID> /F
```

### Contenedores de Docker caídos
```bash
docker start sga_postgres sga_redis sga_rabbitmq sga_minio
```

### Cuenta bloqueada tras intentos fallidos
```bash
docker exec -it sga_redis redis-cli FLUSHALL
```

### Token expirado / error 401 inesperado
El token dura 15 minutos. Si reiniciaste el backend, todos los tokens emitidos antes del reinicio quedan inválidos (las claves RS256 se regeneran en cada arranque).

### Conflicto entre PostgreSQL nativo (Windows) y el contenedor Docker
```powershell
netstat -ano | findstr :5432
Stop-Service -Name "postgresql-x64-17"
Set-Service -Name "postgresql-x64-17" -StartupType Manual
```

### Error 401 inesperado en un endpoint que debería funcionar
El manejador global de excepciones traduce cada error a su código HTTP correcto (404, 400, 403, 500). Si ves un 401 fuera del flujo de autenticación, revisa el log (logs/sga.json) usando el traceId de la respuesta de error.

---

## Detener el proyecto

```bash
docker compose down
```

---

## Trabajo futuro (fuera del alcance actual)

- HTTPS/TLS en producción: pendiente el despliegue a un servidor con dominio público (Hostinger + DuckDNS + Let's Encrypt/Certbot detrás de Nginx).
- Particionamiento de las tablas reales: actualmente solo existe como demostración aislada.
- Extender la jerarquía de excepciones de negocio al resto de servicios (actualmente cubre EstudianteService y CalificacionService).
- Compartir las claves RS256 entre instancias si se despliegan múltiples réplicas del backend en paralelo.