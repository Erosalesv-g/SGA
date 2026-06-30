package com.sga.unemi.dto;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Estado de un trabajo de generación masiva de boletines, expuesto al
 * frontend para mostrar el progreso (por ejemplo, una barra de avance
 * "120/500 procesados").
 */
public class TrabajoBoletinMasivoResponse {

    private UUID id;
    private String nivel;
    private String estado;
    private int totalEstudiantes;
    private int procesados;
    private int fallidos;
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    public TrabajoBoletinMasivoResponse(UUID id, String nivel, String estado,
                                         int totalEstudiantes, int procesados, int fallidos,
                                         LocalDateTime fechaInicio, LocalDateTime fechaFin) {
        this.id = id;
        this.nivel = nivel;
        this.estado = estado;
        this.totalEstudiantes = totalEstudiantes;
        this.procesados = procesados;
        this.fallidos = fallidos;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
    }

    public UUID getId() { return id; }
    public String getNivel() { return nivel; }
    public String getEstado() { return estado; }
    public int getTotalEstudiantes() { return totalEstudiantes; }
    public int getProcesados() { return procesados; }
    public int getFallidos() { return fallidos; }
    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public LocalDateTime getFechaFin() { return fechaFin; }
}