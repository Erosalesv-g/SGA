package com.sga.unemi.dto;

import java.util.UUID;

public class DocenteResponse {

    private UUID id;
    private String nombre;
    private String email;
    private String cedula;
    private String titulo;
    private String especialidad;
    private boolean activo;

    public DocenteResponse(UUID id, String nombre, String email, String cedula,
                            String titulo, String especialidad, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.cedula = cedula;
        this.titulo = titulo;
        this.especialidad = especialidad;
        this.activo = activo;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getCedula() { return cedula; }
    public String getTitulo() { return titulo; }
    public String getEspecialidad() { return especialidad; }
    public boolean isActivo() { return activo; }
}