package com.sga.unemi.dto;

import java.util.UUID;

public class EstudianteResponse {

    private UUID id;
    private String nombre;
    private String email;
    private String codigo;
    private String nivel;
    private String seccion;
    private boolean activo;

    public EstudianteResponse(UUID id, String nombre, String email, String codigo,
                               String nivel, String seccion, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.codigo = codigo;
        this.nivel = nivel;
        this.seccion = seccion;
        this.activo = activo;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getCodigo() { return codigo; }
    public String getNivel() { return nivel; }
    public String getSeccion() { return seccion; }
    public boolean isActivo() { return activo; }
}