package com.sga.unemi.dto;

import java.util.UUID;

public class EstudianteRequest {

    private String nombre;
    private String email;
    private String password;
    private String codigo;
    private String nivel;
    private String seccion;
    private UUID representanteId;

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }

    public UUID getRepresentanteId() { return representanteId; }
    public void setRepresentanteId(UUID representanteId) { this.representanteId = representanteId; }
}