-- ============================================================================
-- Demostracion de particionamiento por año lectivo (RNF-0012)
-- ============================================================================
-- Este script NO modifica las tablas reales de la aplicacion (calificaciones,
-- etc.). Crea una tabla de demostracion separada para probar la tecnica de
-- particionamiento de PostgreSQL, util cuando el volumen de datos historicos
-- acumulados a lo largo de varios años lectivos hace que las consultas sobre
-- una sola tabla gigante se vuelvan lentas.
--
-- Como funciona: en vez de una sola tabla con todos los registros mezclados,
-- PostgreSQL divide la tabla en "particiones" fisicas separadas (una por año
-- lectivo). Para las consultas, todo se sigue viendo como una sola tabla;
-- pero si el motor sabe que la consulta solo necesita un año especifico,
-- puede ignorar por completo las particiones de otros años en lugar de
-- escanear todo ("partition pruning").
--
-- Esta version incluye unicamente la particion del año lectivo actual
-- (2026-2027). Agregar particiones para años anteriores o futuros es tan
-- simple como ejecutar otro CREATE TABLE ... PARTITION OF, sin tocar las
-- particiones existentes.
--
-- Uso: ejecutar manualmente para fines de demostracion.
--   Get-Content scripts\particionamiento_demo.sql | docker exec -i sga_postgres psql -U postgres -d sga_db
--
-- IMPORTANTE: la tabla actual "calificaciones" usada por la aplicacion NO usa
-- particionamiento. Migrarla a este esquema seria el siguiente paso cuando
-- el volumen de datos historicos lo amerite (ver seccion "Trabajo futuro"
-- del README).

DROP TABLE IF EXISTS calificaciones_particionada_demo CASCADE;

-- Tabla maestra particionada por rango de fecha (fecha_registro debe ser
-- parte de la clave primaria: PostgreSQL lo exige para tablas particionadas)
CREATE TABLE calificaciones_particionada_demo (
    id UUID DEFAULT gen_random_uuid(),
    estudiante_id UUID NOT NULL,
    materia_id UUID NOT NULL,
    valor NUMERIC(4,2) NOT NULL,
    tipo VARCHAR(20) NOT NULL,
    fecha_registro DATE NOT NULL,
    PRIMARY KEY (id, fecha_registro)
) PARTITION BY RANGE (fecha_registro);

-- Particion del año lectivo actual (2026-2027)
CREATE TABLE calificaciones_2026_2027
    PARTITION OF calificaciones_particionada_demo
    FOR VALUES FROM ('2026-09-01') TO ('2027-08-31');

-- Datos de prueba en la particion actual
INSERT INTO calificaciones_particionada_demo (estudiante_id, materia_id, valor, tipo, fecha_registro)
VALUES
    (gen_random_uuid(), gen_random_uuid(), 9.00, 'EXAMEN', '2026-09-15'),
    (gen_random_uuid(), gen_random_uuid(), 6.75, 'EXAMEN', '2026-10-20'),
    (gen_random_uuid(), gen_random_uuid(), 8.30, 'PARCIAL', '2026-11-05');

-- Verificacion: cuantas filas quedaron en la particion
SELECT 'calificaciones_2026_2027' AS particion, COUNT(*) FROM calificaciones_2026_2027;

-- Demostracion de "partition pruning": al filtrar por fecha del año lectivo
-- actual, PostgreSQL escanea SOLO calificaciones_2026_2027. Esto se confirma
-- viendo "Seq Scan on calificaciones_2026_2027" en el plan de ejecucion.
EXPLAIN SELECT * FROM calificaciones_particionada_demo
WHERE fecha_registro BETWEEN '2026-09-01' AND '2027-08-31';