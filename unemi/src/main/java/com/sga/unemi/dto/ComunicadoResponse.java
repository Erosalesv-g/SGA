package com.sga.unemi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class ComunicadoResponse {

    private UUID id;
    private String titulo;
    private String contenido;
    private UUID remitenteId;
    private String remitenteNombre;
    private String destinatarioRol;
    private LocalDateTime fechaEnvio;

    public ComunicadoResponse(UUID id, String titulo, String contenido,
                               UUID remitenteId, String remitenteNombre,
                               String destinatarioRol, LocalDateTime fechaEnvio) {
        this.id = id;
        this.titulo = titulo;
        this.contenido = contenido;
        this.remitenteId = remitenteId;
        this.remitenteNombre = remitenteNombre;
        this.destinatarioRol = destinatarioRol;
        this.fechaEnvio = fechaEnvio;
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getContenido() { return contenido; }
    public UUID getRemitenteId() { return remitenteId; }
    public String getRemitenteNombre() { return remitenteNombre; }
    public String getDestinatarioRol() { return destinatarioRol; }
    public LocalDateTime getFechaEnvio() { return fechaEnvio; }
}