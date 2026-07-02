package com.sga.unemi.dto;

import java.util.UUID;

public class DocenteJornadaInfo {

    private UUID docenteId;
    private String docenteNombre;
    private String jornada;

    public DocenteJornadaInfo(UUID docenteId, String docenteNombre, String jornada) {
        this.docenteId = docenteId;
        this.docenteNombre = docenteNombre;
        this.jornada = jornada;
    }

    public UUID getDocenteId() { return docenteId; }
    public String getDocenteNombre() { return docenteNombre; }
    public String getJornada() { return jornada; }
}