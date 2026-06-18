package com.sga.unemi.dto;

import java.time.LocalDate;
import java.util.UUID;

public class CalificacionRequest {

    private Double valor;
    private String tipo;
    private LocalDate fechaRegistro;
    private UUID estudianteId;
    private UUID materiaId;
    private UUID docenteId;

    public Double getValor() { return valor; }
    public void setValor(Double valor) { this.valor = valor; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public void setFechaRegistro(LocalDate fechaRegistro) { this.fechaRegistro = fechaRegistro; }

    public UUID getEstudianteId() { return estudianteId; }
    public void setEstudianteId(UUID estudianteId) { this.estudianteId = estudianteId; }

    public UUID getMateriaId() { return materiaId; }
    public void setMateriaId(UUID materiaId) { this.materiaId = materiaId; }

    public UUID getDocenteId() { return docenteId; }
    public void setDocenteId(UUID docenteId) { this.docenteId = docenteId; }
}