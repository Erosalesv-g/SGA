package com.sga.unemi.dto;

import java.time.LocalTime;
import java.util.UUID;

public class HorarioRequest {

    private UUID docenteId;
    private UUID materiaId;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String aula;
    private String periodo;

    public UUID getDocenteId() { return docenteId; }
    public void setDocenteId(UUID docenteId) { this.docenteId = docenteId; }

    public UUID getMateriaId() { return materiaId; }
    public void setMateriaId(UUID materiaId) { this.materiaId = materiaId; }

    public String getDiaSemana() { return diaSemana; }
    public void setDiaSemana(String diaSemana) { this.diaSemana = diaSemana; }

    public LocalTime getHoraInicio() { return horaInicio; }
    public void setHoraInicio(LocalTime horaInicio) { this.horaInicio = horaInicio; }

    public LocalTime getHoraFin() { return horaFin; }
    public void setHoraFin(LocalTime horaFin) { this.horaFin = horaFin; }

    public String getAula() { return aula; }
    public void setAula(String aula) { this.aula = aula; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }
}