CREATE TABLE notificaciones (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    destinatario_id UUID NOT NULL REFERENCES usuarios(id),
    tipo VARCHAR(50) NOT NULL,
    mensaje VARCHAR(500) NOT NULL,
    referencia_id UUID,
    leida BOOLEAN NOT NULL DEFAULT FALSE,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_notificaciones_destinatario ON notificaciones(destinatario_id);