package com.sga.unemi.dto;

import java.util.List;
import java.util.UUID;

public class MateriaResponse {

    private UUID id;
    private String nombre;
    private String codigo;
    private int creditos;
    private String nivel;
    private UUID docenteId;
    private String docenteNombre;
    private List<DocenteJornadaInfo> docentesPorJornada;

    public MateriaResponse(UUID id, String nombre, String codigo, int creditos, String nivel,
                            UUID docenteId, String docenteNombre,
                            List<DocenteJornadaInfo> docentesPorJornada) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.creditos = creditos;
        this.nivel = nivel;
        this.docenteId = docenteId;
        this.docenteNombre = docenteNombre;
        this.docentesPorJornada = docentesPorJornada;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public int getCreditos() { return creditos; }
    public String getNivel() { return nivel; }
    public UUID getDocenteId() { return docenteId; }
    public String getDocenteNombre() { return docenteNombre; }
    public List<DocenteJornadaInfo> getDocentesPorJornada() { return docentesPorJornada; }
}