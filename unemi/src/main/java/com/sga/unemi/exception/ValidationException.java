package com.sga.unemi.exception;

/**
 * Se lanza cuando una operación viola una regla de validación de negocio
 * (por ejemplo, una calificación fuera de rango, un conflicto de horario,
 * o un período de matrícula inválido). Se traduce a HTTP 400 (Bad Request)
 * por {@link GlobalExceptionHandler}.
 */
public class ValidationException extends AcademicException {

    public ValidationException(String mensaje) {
        super(mensaje);
    }
}