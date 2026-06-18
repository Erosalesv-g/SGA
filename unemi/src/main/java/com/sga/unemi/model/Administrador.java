package com.sga.unemi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "administradores")
public class Administrador extends Usuario {

    @Column(nullable = false)
    private String codigoAdmin;

    // Getters y Setters
    public String getCodigoAdmin() { return codigoAdmin; }
    public void setCodigoAdmin(String codigoAdmin) { 
        this.codigoAdmin = codigoAdmin; 
    }
}