package com.sga.unemi.model;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "materias")
public class Materia {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String codigo;

    @Column(nullable = false)
    private int creditos;

    @ManyToOne
    @JoinColumn(name = "docente_id")
    private Docente docente;

    @Column
    private String nivel;

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public int getCreditos() { return creditos; }
    public void setCreditos(int creditos) { this.creditos = creditos; }

    public Docente getDocente() { return docente; }
    public void setDocente(Docente docente) { this.docente = docente; }
}