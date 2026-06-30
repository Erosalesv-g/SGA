-- V15: Indice compuesto para optimizar consultas frecuentes de calificaciones
-- (estudiante_id + materia_id), el patron de busqueda mas comun del sistema
-- (boletines, promedios, listados filtrados). PostgreSQL puede usar este
-- indice para resolver estas consultas sin escanear toda la tabla.
CREATE INDEX IF NOT EXISTS idx_calificaciones_estudiante_materia
    ON calificaciones (estudiante_id, materia_id);