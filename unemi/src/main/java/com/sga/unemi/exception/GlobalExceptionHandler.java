package com.sga.unemi.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.NoSuchElementException;

/**
 * Manejador centralizado de excepciones (RNF-0013, sección "Manejo de
 * Errores" del documento de diseño).
 * <p>
 * Captura las excepciones de negocio ({@link AcademicException} y sus
 * subclases) y las traduce a respuestas HTTP estructuradas con código,
 * mensaje y timestamp, en lugar de dejar que Spring devuelva el stack
 * trace crudo o un error genérico 500. También captura
 * {@link NoSuchElementException}, usada en varios servicios del sistema
 * antes de la introducción de la jerarquía de excepciones de negocio.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Maneja {@link ResourceNotFoundException}: el recurso solicitado no
     * existe.
     *
     * @param ex la excepción capturada
     * @return respuesta HTTP 404 con el mensaje de la excepción
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrado(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    /**
     * Maneja {@link UnauthorizedException}: el usuario no tiene permiso
     * para la operación solicitada.
     *
     * @param ex la excepción capturada
     * @return respuesta HTTP 403 con el mensaje de la excepción
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> manejarNoAutorizado(UnauthorizedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse(403, ex.getMessage()));
    }

    /**
     * Maneja {@link ValidationException}: la operación viola una regla de
     * validación de negocio.
     *
     * @param ex la excepción capturada
     * @return respuesta HTTP 400 con el mensaje de la excepción
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(ValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, ex.getMessage()));
    }

    /**
     * Maneja cualquier otra {@link AcademicException} no cubierta por los
     * manejadores más específicos.
     *
     * @param ex la excepción capturada
     * @return respuesta HTTP 400 con el mensaje de la excepción
     */
    @ExceptionHandler(AcademicException.class)
    public ResponseEntity<ErrorResponse> manejarAcademica(AcademicException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(400, ex.getMessage()));
    }

    /**
     * Maneja {@link NoSuchElementException}, usada en varios servicios
     * existentes del sistema para señalar que un recurso no fue
     * encontrado (equivalente semánticamente a
     * {@link ResourceNotFoundException}).
     *
     * @param ex la excepción capturada
     * @return respuesta HTTP 404 con el mensaje de la excepción
     */
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ErrorResponse> manejarElementoNoEncontrado(NoSuchElementException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse(404, ex.getMessage()));
    }

    /**
     * Maneja cualquier excepción no controlada que no encaje en las
     * categorías anteriores, evitando que el cliente reciba un stack
     * trace crudo. El detalle completo se registra en el log del servidor
     * para diagnóstico, pero el cliente recibe un mensaje genérico.
     *
     * @param ex la excepción capturada
     * @return respuesta HTTP 500 con un mensaje genérico
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> manejarGeneral(Exception ex) {
        log.error("Error no controlado: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(500, "Ocurrió un error interno. Intenta de nuevo más tarde."));
    }
}