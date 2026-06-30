-- V13: Agrega columna de control de versión para bloqueo optimista en matrículas (RF-02)
-- La columna 'version' es gestionada automáticamente por JPA (@Version):
-- se incrementa en cada UPDATE, y si dos transacciones concurrentes leen
-- la misma versión e intentan actualizar, la segunda falla con un error de
-- concurrencia en vez de sobrescribir silenciosamente los cambios de la primera.
ALTER TABLE matriculas ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;