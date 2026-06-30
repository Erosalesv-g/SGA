-- V14: Tabla para rastrear trabajos de generacion masiva de boletines (RNF-0011)
-- Extiende el uso de RabbitMQ (ya usado para Comunicados) al procesamiento
-- asincrono de boletines para todos los estudiantes de un nivel, evitando que
-- una peticion HTTP tenga que esperar a que se generen potencialmente
-- cientos de PDFs antes de responder.
CREATE TABLE IF NOT EXISTS trabajos_boletin_masivo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    nivel VARCHAR(50) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE',
    total_estudiantes INT NOT NULL DEFAULT 0,
    procesados INT NOT NULL DEFAULT 0,
    fallidos INT NOT NULL DEFAULT 0,
    solicitado_por UUID NOT NULL REFERENCES usuarios(id),
    fecha_inicio TIMESTAMP NOT NULL DEFAULT now(),
    fecha_fin TIMESTAMP,
    mensaje_error TEXT
);

CREATE INDEX IF NOT EXISTS idx_trabajos_boletin_estado ON trabajos_boletin_masivo(estado);