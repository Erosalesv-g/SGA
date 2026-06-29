# Sistema de Gestión Académica (SGA)

Sistema de gestión académica para la Unidad Educativa Fiscal "Durán", desarrollado con Spring Boot (backend) y React + TypeScript (frontend), configurado como Progressive Web App (PWA) con soporte offline de solo lectura.

## Tecnologías

- **Backend:** Java 21, Spring Boot 3.3, Spring Security, JWT, PostgreSQL, Redis, RabbitMQ, MinIO, Flyway, Resilience4j, OpenPDF
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
| Redis | 6379 | Sesiones y bloqueo por intentos fallidos |
| RabbitMQ | 5672 (AMQP), 15672 (consola web) | Envío asíncrono de comunicados masivos |
| MinIO | 9000 (API), 9001 (consola web) | Almacenamiento de recursos pedagógicos |

Confirma que los 4 estén corriendo con `docker ps`.

### 3. Levantar el backend

```bash
cd unemi
.\mvnw spring-boot:run
```

> En Mac/Linux usa `./mvnw spring-boot:run`

Las 12 migraciones de Flyway se aplican automáticamente al iniciar.

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

La base de datos empieza vacía — no hay ningún usuario para iniciar sesión. Crea el primero manualmente conectado al contenedor de Postgres:

```powershell
docker exec -it sga_postgres psql -U postgres -d sga_db -c 'INSERT INTO usuarios (id, nombre, email, password_hash, rol, activo, intentos_fallidos) VALUES (gen_random_uuid(), ''Administrador'', ''admin@sga.edu.ec'', ''$2b$10$BpH51d3iq9pWHdfLNtYcEuJfgZ/3to/ecTbaoO7ucfVD0nkzru15q'', ''RECTOR'', true, 0);'
```

> **Importante (PowerShell en Windows):** el comando usa comillas simples a propósito. El hash de la contraseña contiene signos `$` (`$2b$10$...`), y PowerShell los interpreta como variables dentro de comillas dobles, lo que corrompe el hash silenciosamente (nos pasó durante el desarrollo). Si necesitas escribir este tipo de comando desde cero, usa siempre comillas simples para el SQL completo, y duplica las comillas simples internas (`''`) para los valores de texto.

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
| **DOCENTE** | Sus propias calificaciones registradas, su horario, asistencia de sus materias, comunicados, subir recursos pedagógicos |
| **ESTUDIANTE** | Solo sus propias calificaciones, asistencia, horario (por nivel), comunicados dirigidos a estudiantes, su boletín/reporte |
| **REPRESENTANTE** | Calificaciones, asistencia y reportes del/los estudiante(s) que representa (vinculado vía `representante_id`) |
| **ORIENTADOR (DECE)** | Estudiantes, asistencia, comunicados, reportes |

---

## Patrones de diseño implementados

| Patrón | Dónde | Propósito |
|---|---|---|
| **Strategy** | `strategy/PromedioStrategyFactory` | Calcula el promedio de calificaciones con una fórmula distinta según el nivel del estudiante (promedio simple para Básica, ponderado 40/30/20/10 por tipo de evaluación para Bachillerato) |
| **Observer** | `observer/CalificacionObserver` | Notifica automáticamente al representante legal cuando se registra una nueva calificación |
| **Circuit Breaker** (Resilience4j) | `service/ComunicadoEventPublisher` | Si RabbitMQ no responde, evita que la creación de un comunicado falle por completo: el comunicado se guarda igual, y el envío de notificaciones queda registrado en el log para reintento manual |

---

## Mensajería asíncrona (RabbitMQ)

Los comunicados masivos no notifican a los destinatarios de forma síncrona (lo cual saturaría el servidor con cientos de representantes). En su lugar:

1. `POST /api/comunicados` guarda el comunicado y publica un evento a RabbitMQ — responde de inmediato.
2. `ComunicadoConsumer` procesa el evento en segundo plano, busca a todos los usuarios del rol destinatario, y les crea una notificación.
3. Si RabbitMQ no está disponible, el Circuit Breaker evita que esto tumbe la petición (ver tabla de patrones arriba).

Consola de administración de RabbitMQ: [http://localhost:15672](http://localhost:15672) (usuario/clave: `guest`/`guest`).

---

## Estructura del proyecto

```
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
│   │   ├── config/         # RabbitMQConfig, WebConfig (rate limiting)
│   │   ├── consumer/       # ComunicadoConsumer (RabbitMQ)
│   │   ├── controller/
│   │   ├── dto/
│   │   ├── model/
│   │   ├── observer/       # Patrón Observer
│   │   ├── repository/
│   │   ├── security/       # JWT, RateLimitInterceptor
│   │   ├── service/        # Incluye ComunicadoEventPublisher (Circuit Breaker)
│   │   └── strategy/       # Patrón Strategy (promedios)
│   └── src/main/resources/
│       ├── db/migration/   # V1 a V12
│       └── application.properties
├── scripts/
│   ├── backup-db.ps1            # Genera un backup de la base de datos
│   ├── restore-db.ps1           # Restaura un backup
│   └── datos_prueba_masivos.sql # Genera 500 estudiantes + calificaciones de prueba
├── backups/                 # Backups generados (ver scripts/backup-db.ps1)
├── docker-compose.yml
└── README.md
```

---

## Funcionalidades implementadas

### Backend
- Autenticación JWT con bloqueo por intentos fallidos (Redis)
- Rate limiting por IP (60 peticiones/minuto en `/api/**`, devuelve `429` al exceder)
- CRUD completo: Usuarios, Estudiantes, Docentes, Materias, Calificaciones, Asistencia, Horarios, Comunicados
- Comunicados masivos vía RabbitMQ con Circuit Breaker (Resilience4j)
- Reportes: boletín de calificaciones (JSON y PDF) y resumen de asistencia por estudiante
- Recursos pedagógicos almacenados en MinIO (subida restringida a rol DOCENTE)
- Validación de rango de notas (0-10)
- Validación de conflicto de horarios (mismo docente, mismo día/hora)
- Relación Representante ↔ Estudiante
- Consultas optimizadas con `JOIN FETCH` para evitar el problema N+1 en listados grandes

### Frontend
- Login responsive con diseño institucional
- Dashboard con sidebar (menú hamburguesa en celular) y menú diferenciado por rol
- Datos filtrados según el rol logueado (no solo el menú, también el contenido)
- Pantalla de Inicio con estadísticas (RECTOR) o comunicados recientes (otros roles)
- Botón de reactivar para estudiantes/docentes desactivados
- PWA instalable con modo offline (cachea la última información cargada; bloquea creación/edición sin conexión)

---

## Scripts de utilidad

### Backup y restauración de la base de datos

```powershell
# Generar un backup (se guarda en backups/, con fecha y hora en el nombre)
.\scripts\backup-db.ps1

# Restaurar un backup especifico
.\scripts\restore-db.ps1 -Archivo "backups\sga_backup_2026-06-29_12-41-45.sql"
```

Los backups de más de 7 días se eliminan automáticamente al correr `backup-db.ps1` de nuevo.

### Generar datos de prueba a escala

Para probar el sistema con cientos de registros (útil para demostrar que soporta la escala documentada, 500-5000 estudiantes):

```powershell
Get-Content scripts\datos_prueba_masivos.sql | docker exec -i sga_postgres psql -U postgres -d sga_db
```

Esto crea 500 estudiantes con sus calificaciones. Requiere que ya exista al menos una materia y un docente en la base (ver el script para los UUIDs esperados, o ajústalos a los tuyos).

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
docker start sga_postgres sga_redis sga_rabbitmq sga_minio
```

### Cuenta bloqueada tras intentos fallidos
El bloqueo se guarda en Redis (no en la base de datos). Para desbloquear manualmente:
```bash
docker exec -it sga_redis redis-cli FLUSHALL
```

### Token expirado / error 401 o 403 inesperado
El token dura 15 minutos. Si pasa este tiempo, vuelve a iniciar sesión.

### Conflicto entre PostgreSQL nativo (Windows) y el contenedor Docker
Si tienes PostgreSQL instalado directamente en Windows (no solo en Docker), puede arrancar como servicio del sistema y "ganar" el puerto 5432 antes que el contenedor, haciendo que el backend se conecte al Postgres equivocado sin ningún error visible (vas a ver datos vacíos o inconsistentes). Para revisar:
```powershell
netstat -ano | findstr :5432
```
Si ves dos procesos distintos escuchando en 5432, identifica cuál es el servicio nativo (`tasklist /FI "PID eq <numero>"`) y detenlo:
```powershell
Stop-Service -Name "postgresql-x64-17"
Set-Service -Name "postgresql-x64-17" -StartupType Manual
```
(Ajusta el nombre del servicio si tu versión de PostgreSQL es distinta.)

### Error 401 que en realidad es otro problema
Algunos errores del backend (excepciones de SQL, columnas faltantes, etc.) pueden devolver código 401 en vez del código real. Si un endpoint que debería funcionar da 401 de forma inconsistente, revisa el log del backend directamente — el mensaje de error real suele estar ahí, no en la respuesta HTTP.

---

## Detener el proyecto

```bash
docker compose down
```

Para detener el backend y frontend, `Ctrl + C` en cada terminal.

---

## Trabajo futuro (fuera del alcance actual)

- **HTTPS/TLS**: no configurado en desarrollo local. Antes de desplegar en un servidor con dominio público, configurar un certificado (por ejemplo con Let's Encrypt/Certbot detrás de un proxy Nginx).
- **RabbitMQ para boletines y alertas masivas**: actualmente solo Comunicados usa la cola; el documento de diseño también contempla extenderlo a la generación de boletines y alertas de asistencia masivas.
- **Backups automatizados por cron/Task Scheduler**: los scripts ya existen (`scripts/backup-db.ps1`), falta programarlos para que corran solos en un horario fijo.