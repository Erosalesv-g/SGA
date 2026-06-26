CREATE TABLE auditoria_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id UUID NOT NULL REFERENCES usuarios(id),
    usuario_nombre VARCHAR(255) NOT NULL,
    accion VARCHAR(20) NOT NULL,
    entidad VARCHAR(50) NOT NULL,
    entidad_id UUID,
    descripcion TEXT,
    fecha TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX idx_auditoria_fecha ON auditoria_log(fecha DESC);