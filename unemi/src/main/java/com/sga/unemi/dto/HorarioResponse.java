package com.sga.unemi.dto;

import java.time.LocalTime;
import java.util.UUID;

public class HorarioResponse {

    private UUID id;
    private UUID docenteId;
    private String docenteNombre;
    private UUID materiaId;
    private String materiaNombre;
    private String diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private String aula;
    private String periodo;

    public HorarioResponse(UUID id, UUID docenteId, String docenteNombre,
                            UUID materiaId, String materiaNombre,
                            String diaSemana, LocalTime horaInicio, LocalTime horaFin,
                            String aula, String periodo) {
        this.id = id;
        this.docenteId = docenteId;
        this.docenteNombre = docenteNombre;
        this.materiaId = materiaId;
        this.materiaNombre = materiaNombre;
        this.diaSemana = diaSemana;
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.aula = aula;
        this.periodo = periodo;
    }

    public UUID getId() { return id; }
    public UUID getDocenteId() { return docenteId; }
    public String getDocenteNombre() { return docenteNombre; }
    public UUID getMateriaId() { return materiaId; }
    public String getMateriaNombre() { return materiaNombre; }
    public String getDiaSemana() { return diaSemana; }
    public LocalTime getHoraInicio() { return horaInicio; }
    public LocalTime getHoraFin() { return horaFin; }
    public String getAula() { return aula; }
    public String getPeriodo() { return periodo; }
}