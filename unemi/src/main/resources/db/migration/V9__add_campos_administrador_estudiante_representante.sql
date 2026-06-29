ALTER TABLE administradores ADD COLUMN IF NOT EXISTS codigo_admin VARCHAR(50);
 
UPDATE administradores SET codigo_admin = 'ADM-' || id WHERE codigo_admin IS NULL;
 
ALTER TABLE administradores ALTER COLUMN codigo_admin SET NOT NULL;
 
ALTER TABLE estudiantes ADD COLUMN IF NOT EXISTS codigo VARCHAR(50);
ALTER TABLE estudiantes ADD COLUMN IF NOT EXISTS nivel VARCHAR(20);
ALTER TABLE estudiantes ADD COLUMN IF NOT EXISTS seccion VARCHAR(10);
 
ALTER TABLE estudiantes ALTER COLUMN codigo SET NOT NULL;
ALTER TABLE estudiantes ALTER COLUMN nivel SET NOT NULL;
ALTER TABLE estudiantes ALTER COLUMN seccion SET NOT NULL;
 
ALTER TABLE representantes_legales ADD COLUMN IF NOT EXISTS relacion_con_estudiante VARCHAR(50);
ALTER TABLE representantes_legales ADD COLUMN IF NOT EXISTS estudiante_id UUID;
 
ALTER TABLE representantes_legales ALTER COLUMN relacion_con_estudiante SET NOT NULL;
 
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_representante_estudiante'
    ) THEN
        ALTER TABLE representantes_legales
            ADD CONSTRAINT fk_representante_estudiante
            FOREIGN KEY (estudiante_id) REFERENCES estudiantes(id);
    END IF;
END $$;