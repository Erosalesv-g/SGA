package com.sga.unemi.dto;

import com.sga.unemi.model.EstadoMatricula;
import java.time.LocalDateTime;
import java.util.UUID;

public class MatriculaResponse {

    private UUID id;
    private UUID estudianteId;
    private String estudianteNombre;
    private String periodo;
    private LocalDateTime fechaMatricula;
    private EstadoMatricula estado;
    private String observaciones;

    public MatriculaResponse(UUID id, UUID estudianteId, String estudianteNombre,
                              String periodo, LocalDateTime fechaMatricula,
                              EstadoMatricula estado, String observaciones) {
        this.id = id;
        this.estudianteId = estudianteId;
        this.estudianteNombre = estudianteNombre;
        this.periodo = periodo;
        this.fechaMatricula = fechaMatricula;
        this.estado = estado;
        this.observaciones = observaciones;
    }

    public UUID getId() { return id; }
    public UUID getEstudianteId() { return estudianteId; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public String getPeriodo() { return periodo; }
    public LocalDateTime getFechaMatricula() { return fechaMatricula; }
    public EstadoMatricula getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }
}