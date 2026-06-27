package com.sga.unemi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "estudiantes")
public class Estudiante extends Usuario {

    @Column(nullable = false)
    private String codigo;

    @Column(nullable = false)
    private String nivel;

    @Column(nullable = false)
    private String seccion;

    @ManyToOne
    @JoinColumn(name = "representante_id")
    @JsonIgnoreProperties("estudiante")
    private Usuario representante;

    public Usuario getRepresentante() { return representante; }
    public void setRepresentante(Usuario representante) { this.representante = representante; }

    // Getters y Setters
    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }
}