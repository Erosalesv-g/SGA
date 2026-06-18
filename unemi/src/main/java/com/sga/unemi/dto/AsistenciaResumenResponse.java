package com.sga.unemi.dto;

import java.util.UUID;

public class AsistenciaResumenResponse {

    private UUID estudianteId;
    private String estudianteNombre;
    private long totalPresente;
    private long totalAusente;
    private long totalJustificado;
    private long totalTardanza;
    private double porcentajeAsistencia;

    public AsistenciaResumenResponse(UUID estudianteId, String estudianteNombre,
                                      long totalPresente, long totalAusente,
                                      long totalJustificado, long totalTardanza,
                                      double porcentajeAsistencia) {
        this.estudianteId = estudianteId;
        this.estudianteNombre = estudianteNombre;
        this.totalPresente = totalPresente;
        this.totalAusente = totalAusente;
        this.totalJustificado = totalJustificado;
        this.totalTardanza = totalTardanza;
        this.porcentajeAsistencia = porcentajeAsistencia;
    }

    public UUID getEstudianteId() { return estudianteId; }
    public String getEstudianteNombre() { return estudianteNombre; }
    public long getTotalPresente() { return totalPresente; }
    public long getTotalAusente() { return totalAusente; }
    public long getTotalJustificado() { return totalJustificado; }
    public long getTotalTardanza() { return totalTardanza; }
    public double getPorcentajeAsistencia() { return porcentajeAsistencia; }
}