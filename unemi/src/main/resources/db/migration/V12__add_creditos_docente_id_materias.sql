ALTER TABLE materias ADD COLUMN IF NOT EXISTS creditos INT;
ALTER TABLE materias ADD COLUMN IF NOT EXISTS docente_id UUID;
 
UPDATE materias SET creditos = 1 WHERE creditos IS NULL;
ALTER TABLE materias ALTER COLUMN creditos SET NOT NULL;
 
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint WHERE conname = 'fk_materia_docente'
    ) THEN
        ALTER TABLE materias
            ADD CONSTRAINT fk_materia_docente
            FOREIGN KEY (docente_id) REFERENCES docentes(id);
    END IF;
END $$;