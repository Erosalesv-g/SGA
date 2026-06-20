ALTER TABLE estudiantes
ADD COLUMN representante_id UUID,
ADD CONSTRAINT fk_estudiante_representante FOREIGN KEY (representante_id) REFERENCES usuarios(id);