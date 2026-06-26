package com.sga.unemi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuditoriaLogResponse {

    private UUID id;
    private String usuarioNombre;
    private String accion;
    private String entidad;
    private UUID entidadId;
    private String descripcion;
    private LocalDateTime fecha;

    public AuditoriaLogResponse(UUID id, String usuarioNombre, String accion, String entidad,
                                 UUID entidadId, String descripcion, LocalDateTime fecha) {
        this.id = id;
        this.usuarioNombre = usuarioNombre;
        this.accion = accion;
        this.entidad = entidad;
        this.entidadId = entidadId;
        this.descripcion = descripcion;
        this.fecha = fecha;
    }

    public UUID getId() { return id; }
    public String getUsuarioNombre() { return usuarioNombre; }
    public String getAccion() { return accion; }
    public String getEntidad() { return entidad; }
    public UUID getEntidadId() { return entidadId; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFecha() { return fecha; }
}