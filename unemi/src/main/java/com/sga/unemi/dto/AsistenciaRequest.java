package com.sga.unemi.dto;

import java.time.LocalDate;
import java.util.UUID;

public class AsistenciaRequest {

    private LocalDate fecha;
    private String estado;
    private UUID estudianteId;
    private UUID materiaId;

    public LocalDate getFecha() { return fecha; }
    public void setFecha(LocalDate fecha) { this.fecha = fecha; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public UUID getEstudianteId() { return estudianteId; }
    public void setEstudianteId(UUID estudianteId) { this.estudianteId = estudianteId; }

    public UUID getMateriaId() { return materiaId; }
    public void setMateriaId(UUID materiaId) { this.materiaId = materiaId; }
}