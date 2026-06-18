package com.sga.unemi.dto;

import java.util.UUID;

public class ComunicadoRequest {

    private String titulo;
    private String contenido;
    private UUID remitenteId;
    private String destinatarioRol;

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getContenido() { return contenido; }
    public void setContenido(String contenido) { this.contenido = contenido; }

    public UUID getRemitenteId() { return remitenteId; }
    public void setRemitenteId(UUID remitenteId) { this.remitenteId = remitenteId; }

    public String getDestinatarioRol() { return destinatarioRol; }
    public void setDestinatarioRol(String destinatarioRol) { this.destinatarioRol = destinatarioRol; }
}