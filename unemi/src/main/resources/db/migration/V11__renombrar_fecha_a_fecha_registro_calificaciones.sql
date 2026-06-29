ALTER TABLE calificaciones RENAME COLUMN fecha TO fecha_registro;
 
ALTER TABLE calificaciones ALTER COLUMN periodo DROP NOT NULL;