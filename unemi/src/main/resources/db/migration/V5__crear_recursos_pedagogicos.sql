CREATE TABLE recursos_pedagogicos (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    titulo VARCHAR(255) NOT NULL,
    descripcion TEXT,
    nombre_archivo VARCHAR(255) NOT NULL,
    nombre_archivo_minio VARCHAR(255) NOT NULL,
    tipo_archivo VARCHAR(100),
    tamano_bytes BIGINT,
    materia_id UUID NOT NULL REFERENCES materias(id),
    docente_id UUID NOT NULL REFERENCES usuarios(id),
    fecha_publicacion TIMESTAMP NOT NULL DEFAULT now()
);