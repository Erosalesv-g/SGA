package com.sga.unemi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "representantes_legales")
public class RepresentanteLegal extends Usuario {

    @Column(nullable = false)
    private String relacionConEstudiante;

    @ManyToOne
    @JoinColumn(name = "estudiante_id")
    @JsonIgnoreProperties("representante")
    private Estudiante estudiante;

    // Getters y Setters
    public String getRelacionConEstudiante() { 
        return relacionConEstudiante; 
    }
    public void setRelacionConEstudiante(String relacionConEstudiante) { 
        this.relacionConEstudiante = relacionConEstudiante; 
    }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { 
        this.estudiante = estudiante; 
    }
}