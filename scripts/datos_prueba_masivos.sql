INSERT INTO usuarios (id, nombre, email, password_hash, rol, activo, intentos_fallidos)
SELECT
    gen_random_uuid(),
    'Estudiante Prueba ' || i,
    'estudiante.prueba' || i || '@sga.edu.ec',
    '$2b$10$BpH51d3iq9pWHdfLNtYcEuJfgZ/3to/ecTbaoO7ucfVD0nkzru15q',
    'ESTUDIANTE',
    true,
    0
FROM generate_series(1, 500) AS i;

INSERT INTO estudiantes (id, codigo, nivel, seccion, numero_matricula)
SELECT
    u.id,
    'EST-PRUEBA-' || LPAD((ROW_NUMBER() OVER (ORDER BY u.email))::text, 5, '0'),
    CASE WHEN (ROW_NUMBER() OVER (ORDER BY u.email)) % 2 = 0 THEN '3°' ELSE '9no' END,
    CASE WHEN (ROW_NUMBER() OVER (ORDER BY u.email)) % 3 = 0 THEN 'B' ELSE 'A' END,
    'MAT-PRUEBA-' || LPAD((ROW_NUMBER() OVER (ORDER BY u.email))::text, 5, '0')
FROM usuarios u
WHERE u.email LIKE 'estudiante.prueba%@sga.edu.ec';

INSERT INTO calificaciones (estudiante_id, materia_id, docente_id, valor, tipo, fecha_registro)
SELECT
    e.id,
    '101bf2b6-7e75-4e51-abec-ecc67f8fb469',
    '4d2d94e3-6be3-4fb7-8618-58668ecb1838',
    ROUND((RANDOM() * 10)::numeric, 2),
    'EXAMEN',
    CURRENT_DATE
FROM estudiantes e
JOIN usuarios u ON u.id = e.id
WHERE u.email LIKE 'estudiante.prueba%@sga.edu.ec';

SELECT COUNT(*) AS estudiantes_prueba_creados FROM usuarios WHERE email LIKE 'estudiante.prueba%@sga.edu.ec';
SELECT COUNT(*) AS calificaciones_prueba_creadas FROM calificaciones c
    JOIN usuarios u ON u.id = c.estudiante_id
    WHERE u.email LIKE 'estudiante.prueba%@sga.edu.ec';