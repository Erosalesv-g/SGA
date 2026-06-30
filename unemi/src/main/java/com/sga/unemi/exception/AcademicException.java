package com.sga.unemi.exception;

/**
 * Excepción base de negocio del sistema (RNF-0013, sección "Manejo de
 * Errores" del documento de diseño).
 * <p>
 * Todas las excepciones específicas del dominio académico
 * ({@link ResourceNotFoundException}, {@link UnauthorizedException},
 * {@link ValidationException}) heredan de esta clase, lo que permite que
 * {@link GlobalExceptionHandler} las capture de forma centralizada y las
 * traduzca a respuestas HTTP estructuradas y consistentes.
 */
public class AcademicException extends RuntimeException {

    public AcademicException(String mensaje) {
        super(mensaje);
    }

    public AcademicException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}