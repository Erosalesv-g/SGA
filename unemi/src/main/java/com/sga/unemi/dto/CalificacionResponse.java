package com.sga.unemi.dto;

import java.time.LocalDate;
import java.util.UUID;

public class CalificacionResponse {

    private UUID id;
    private Double valor;
    private String tipo;
    private LocalDate fechaRegistro;
    private UUID estudianteId;
    private String estudianteNombre;
    private UUID materiaId;
    private String materiaNombre;
    private UUID docenteId;
    private String docenteNombre;

    public CalificacionResponse(UUID id, Double valor, String tipo, LocalDate fechaRegistro,
                                 UUID estudianteId, String estudianteNombre,
                                 UUID materiaId, String materiaNombre,
                                 UUID docenteId, String docenteNombre) {
        this.id = id;
        this.valor = valor;
        this.tipo = tipo;
        this.fechaRegistro = fechaRegistro;
        this.estudianteId = estudianteId;
        this.estudianteNombre = estudianteNombre;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
        this.docenteId = docenteId;
        this.docenteNombre = docenteNombre;
    }

    public UUID getId() { return id; }
    public Double getValor() { return valor; }
    public String getTipo() { return tipo; }
    public LocalDate getFechaRegistro() { return fechaRegistro; }
    public UUID getEstudianteId() { return estudianteId; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public UUID getMateriaId() { return materiaId; }
    public String getMateriaNombre() { return materiaNombre; }
    public UUID getDocenteId() { return docenteId; }
    public String getDocenteNombre() { return docenteNombre; }
}