package com.sga.unemi.dto;

import java.util.UUID;

public class MateriaResponse {

    private UUID id;
    private String nombre;
    private String codigo;
    private int creditos;
    private UUID docenteId;
    private String docenteNombre;

    public MateriaResponse(UUID id, String nombre, String codigo, int creditos,
                            UUID docenteId, String docenteNombre) {
        this.id = id;
        this.nombre = nombre;
        this.codigo = codigo;
        this.creditos = creditos;
        this.docenteId = docenteId;
        this.docenteNombre = docenteNombre;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getCodigo() { return codigo; }
    public int getCreditos() { return creditos; }
    public UUID getDocenteId() { return docenteId; }
    public String getDocenteNombre() { return docenteNombre; }
}