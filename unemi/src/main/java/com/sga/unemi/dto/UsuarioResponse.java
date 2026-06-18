package com.sga.unemi.dto;

import java.util.UUID;

public class UsuarioResponse {

    private UUID id;
    private String nombre;
    private String email;
    private String rol;
    private boolean activo;

    public UsuarioResponse(UUID id, String nombre, String email, String rol, boolean activo) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.rol = rol;
        this.activo = activo;
    }

    public UUID getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getRol() { return rol; }
    public boolean isActivo() { return activo; }
}