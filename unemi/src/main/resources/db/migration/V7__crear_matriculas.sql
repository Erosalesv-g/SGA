CREATE TABLE matriculas (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL REFERENCES estudiantes(id),
    periodo VARCHAR(20) NOT NULL,
    fecha_matricula TIMESTAMP NOT NULL DEFAULT now(),
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',
    observaciones TEXT,
    UNIQUE (estudiante_id, periodo)
);

CREATE INDEX idx_matriculas_estudiante ON matriculas(estudiante_id);
CREATE INDEX idx_matriculas_periodo ON matriculas(periodo);