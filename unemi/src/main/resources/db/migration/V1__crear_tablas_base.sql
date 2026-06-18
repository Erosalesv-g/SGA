CREATE TABLE IF NOT EXISTS usuarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(30) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    intentos_fallidos INT NOT NULL DEFAULT 0,
    bloqueado_hasta TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS docentes (
    id UUID PRIMARY KEY REFERENCES usuarios(id),
    titulo VARCHAR(100),
    especialidad VARCHAR(100),
    telefono VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS estudiantes (
    id UUID PRIMARY KEY REFERENCES usuarios(id),
    fecha_nacimiento DATE,
    direccion VARCHAR(200),
    telefono VARCHAR(20),
    numero_matricula VARCHAR(20) UNIQUE
);

CREATE TABLE IF NOT EXISTS representantes_legales (
    id UUID PRIMARY KEY REFERENCES usuarios(id),
    telefono VARCHAR(20),
    parentesco VARCHAR(50)
);

CREATE TABLE IF NOT EXISTS administradores (
    id UUID PRIMARY KEY REFERENCES usuarios(id),
    cargo VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS materias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre VARCHAR(100) NOT NULL,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nivel VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS horarios (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    docente_id UUID NOT NULL REFERENCES docentes(id),
    materia_id UUID NOT NULL REFERENCES materias(id),
    dia_semana VARCHAR(15) NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fin TIME NOT NULL,
    aula VARCHAR(20),
    periodo VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS calificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id),
    materia_id UUID NOT NULL REFERENCES materias(id),
    docente_id UUID NOT NULL REFERENCES docentes(id),
    valor DECIMAL(4,2) NOT NULL CHECK (valor >= 0 AND valor <= 10),
    tipo VARCHAR(30) NOT NULL,
    periodo VARCHAR(20) NOT NULL,
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS asistencias (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id),
    materia_id UUID NOT NULL REFERENCES materias(id),
    fecha DATE NOT NULL DEFAULT CURRENT_DATE,
    estado VARCHAR(1) NOT NULL CHECK (estado IN ('P','A','J','T')),
    observacion VARCHAR(200)
);

CREATE TABLE IF NOT EXISTS comunicados (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(200) NOT NULL,
    contenido TEXT NOT NULL,
    remitente_id UUID NOT NULL REFERENCES usuarios(id),
    destinatario_rol VARCHAR(30),
    fecha_envio TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS auditoria (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID REFERENCES usuarios(id),
    operacion VARCHAR(10) NOT NULL,
    tabla_afectada VARCHAR(50) NOT NULL,
    descripcion TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT NOW(),
    ip_address VARCHAR(45)
);

CREATE INDEX IF NOT EXISTS idx_calificaciones_estudiante ON calificaciones(estudiante_id);
CREATE INDEX IF NOT EXISTS idx_asistencias_estudiante ON asistencias(estudiante_id, fecha);
CREATE INDEX IF NOT EXISTS idx_usuarios_email ON usuarios(email);
CREATE INDEX IF NOT EXISTS idx_horarios_docente ON horarios(docente_id);