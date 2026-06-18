package com.sga.unemi.dto;

import java.time.LocalDate;
import java.util.UUID;

public class AsistenciaResponse {

    private UUID id;
    private LocalDate fecha;
    private String estado;
    private UUID estudianteId;
    private String estudianteNombre;
    private UUID materiaId;
    private String materiaNombre;

    public AsistenciaResponse(UUID id, LocalDate fecha, String estado,
                               UUID estudianteId, String estudianteNombre,
                               UUID materiaId, String materiaNombre) {
        this.id = id;
        this.fecha = fecha;
        this.estado = estado;
        this.estudianteId = estudianteId;
        this.estudianteNombre = estudianteNombre;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
    }

    public UUID getId() { return id; }
    public LocalDate getFecha() { return fecha; }
    public String getEstado() { return estado; }
    public UUID getEstudianteId() { return estudianteId; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public UUID getMateriaId() { return materiaId; }
    public String getMateriaNombre() { return materiaNombre; }
}