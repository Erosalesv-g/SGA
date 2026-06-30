package com.sga.unemi.exception;

import java.time.LocalDateTime;

/**
 * Estructura uniforme de respuesta para cualquier error capturado por
 * {@link GlobalExceptionHandler}: código de error, mensaje descriptivo y
 * marca de tiempo, conforme a la sección "Manejo de Errores" del documento
 * de diseño.
 */
public class ErrorResponse {

    private int codigo;
    private String mensaje;
    private LocalDateTime timestamp;

    public ErrorResponse(int codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
        this.timestamp = LocalDateTime.now();
    }

    public int getCodigo() { return codigo; }
    public String getMensaje() { return mensaje; }
    public LocalDateTime getTimestamp() { return timestamp; }
}