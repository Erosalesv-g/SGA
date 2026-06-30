-- V16: Auditoría automática a nivel de base de datos mediante triggers de
-- PostgreSQL (sección "Persistencia de Datos" del documento de diseño).
--
-- Esta auditoría a nivel de trigger es complementaria a la auditoría a
-- nivel de aplicación que ya existe (tabla auditoria_log, llenada por
-- AuditoriaLogService): el trigger captura automáticamente el TIPO DE
-- OPERACION (INSERT/UPDATE/DELETE) y el TIMESTAMP exacto de cada cambio en
-- la tabla, incluso si el cambio se hiciera directamente en la base de
-- datos sin pasar por la aplicación (por ejemplo, en mantenimiento manual).
--
-- Nota de diseño: como todas las conexiones desde el backend usan el mismo
-- rol de PostgreSQL (no un rol por usuario de la aplicación), el trigger
-- no puede identificar a que usuario especifico de la app corresponde el
-- cambio; esa trazabilidad detallada (quien, con que rol, en que contexto)
-- la sigue cubriendo la auditoria a nivel de aplicacion (auditoria_log).
-- El trigger aporta una capa adicional de garantia: ningun cambio en la
-- tabla, sea cual sea su origen, pasa desapercibido.

CREATE TABLE IF NOT EXISTS auditoria_triggers (
    id BIGSERIAL PRIMARY KEY,
    tabla VARCHAR(50) NOT NULL,
    operacion VARCHAR(10) NOT NULL,
    registro_id UUID,
    rol_conexion VARCHAR(50) NOT NULL,
    fecha TIMESTAMP NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_auditoria_triggers_tabla ON auditoria_triggers(tabla);
CREATE INDEX IF NOT EXISTS idx_auditoria_triggers_fecha ON auditoria_triggers(fecha DESC);

-- Funcion generica de trigger: registra cualquier INSERT, UPDATE o DELETE
-- en la tabla a la que se asocie.
CREATE OR REPLACE FUNCTION fn_auditoria_automatica()
RETURNS TRIGGER AS $$
BEGIN
    IF (TG_OP = 'DELETE') THEN
        INSERT INTO auditoria_triggers (tabla, operacion, registro_id, rol_conexion)
        VALUES (TG_TABLE_NAME, TG_OP, OLD.id, current_user);
        RETURN OLD;
    ELSE
        INSERT INTO auditoria_triggers (tabla, operacion, registro_id, rol_conexion)
        VALUES (TG_TABLE_NAME, TG_OP, NEW.id, current_user);
        RETURN NEW;
    END IF;
END;
$$ LANGUAGE plpgsql;

-- Trigger aplicado a "calificaciones", la entidad mas critica del sistema
-- (RF-03). El mismo patron (DROP TRIGGER IF EXISTS + CREATE TRIGGER) puede
-- replicarse para otras tablas criticas como matriculas o usuarios.
DROP TRIGGER IF EXISTS trg_auditoria_calificaciones ON calificaciones;
CREATE TRIGGER trg_auditoria_calificaciones
    AFTER INSERT OR UPDATE OR DELETE ON calificaciones
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria_automatica();

-- Trigger aplicado a "matriculas" (RF-02), tambien critica para la
-- integridad academica del estudiante.
DROP TRIGGER IF EXISTS trg_auditoria_matriculas ON matriculas;
CREATE TRIGGER trg_auditoria_matriculas
    AFTER INSERT OR UPDATE OR DELETE ON matriculas
    FOR EACH ROW EXECUTE FUNCTION fn_auditoria_automatica();