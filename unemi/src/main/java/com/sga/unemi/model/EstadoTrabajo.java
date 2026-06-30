package com.sga.unemi.model;

/**
 * Estado del procesamiento de un trabajo de generación masiva de boletines.
 *
 * @see TrabajoBoletinMasivo
 */
public enum EstadoTrabajo {
    /** El trabajo se creó pero el consumidor de RabbitMQ aún no lo procesó. */
    PENDIENTE,
    /** El consumidor está generando los boletines en este momento. */
    PROCESANDO,
    /** Todos los boletines se generaron exitosamente. */
    COMPLETADO,
    /** El trabajo terminó con al menos un boletín fallido (ver {@code fallidos}). */
    COMPLETADO_CON_ERRORES,
    /** El trabajo falló por completo antes de poder procesar ningún boletín. */
    FALLIDO
}