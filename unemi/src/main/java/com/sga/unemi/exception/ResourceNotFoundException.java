package com.sga.unemi.exception;

/**
 * Se lanza cuando se solicita un recurso (estudiante, materia, calificación,
 * etc.) que no existe en la base de datos. Se traduce a HTTP 404 (Not Found)
 * por {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends AcademicException {

    public ResourceNotFoundException(String mensaje) {
        super(mensaje);
    }
}