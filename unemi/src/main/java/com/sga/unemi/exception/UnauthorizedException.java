package com.sga.unemi.exception;

/**
 * Se lanza cuando un usuario autenticado intenta realizar una operación
 * para la que no tiene permiso (independientemente de las verificaciones
 * de rol de Spring Security, para casos de lógica de negocio más
 * específica, como un docente intentando editar la calificación de una
 * materia que no le pertenece). Se traduce a HTTP 403 (Forbidden) por
 * {@link GlobalExceptionHandler}.
 */
public class UnauthorizedException extends AcademicException {

    public UnauthorizedException(String mensaje) {
        super(mensaje);
    }
}