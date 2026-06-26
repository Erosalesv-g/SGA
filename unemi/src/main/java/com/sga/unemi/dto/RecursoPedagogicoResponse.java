package com.sga.unemi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public class RecursoPedagogicoResponse {

    private UUID id;
    private String titulo;
    private String descripcion;
    private String nombreArchivo;
    private String tipoArchivo;
    private Long tamanoBytes;
    private UUID materiaId;
    private String materiaNombre;
    private UUID docenteId;
    private String docenteNombre;
    private LocalDateTime fechaPublicacion;

    public RecursoPedagogicoResponse(UUID id, String titulo, String descripcion,
                                      String nombreArchivo, String tipoArchivo, Long tamanoBytes,
                                      UUID materiaId, String materiaNombre,
                                      UUID docenteId, String docenteNombre,
                                      LocalDateTime fechaPublicacion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.nombreArchivo = nombreArchivo;
        this.tipoArchivo = tipoArchivo;
        this.tamanoBytes = tamanoBytes;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
        this.docenteId = docenteId;
        this.docenteNombre = docenteNombre;
        this.fechaPublicacion = fechaPublicacion;
    }

    public UUID getId() { return id; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public String getNombreArchivo() { return nombreArchivo; }
    public String getTipoArchivo() { return tipoArchivo; }
    public Long getTamanoBytes() { return tamanoBytes; }
    public UUID getMateriaId() { return materiaId; }
    public String getMateriaNombre() { return materiaNombre; }
    public UUID getDocenteId() { return docenteId; }
    public String getDocenteNombre() { return docenteNombre; }
    public LocalDateTime getFechaPublicacion() { return fechaPublicacion; }
}