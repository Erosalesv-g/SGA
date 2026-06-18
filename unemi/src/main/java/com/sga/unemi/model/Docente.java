package com.sga.unemi.model;

import jakarta.persistence.*;

@Entity
@Table(name = "docentes")
public class Docente extends Usuario {

    @Column(nullable = false)
    private String cedula;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String especialidad;

    // Getters y Setters
    public String getCedula() { return cedula; }
    public void setCedula(String cedula) { this.cedula = cedula; }

    public String getTitulo() { return titulo; }
    public void setTitulo(String titulo) { this.titulo = titulo; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}