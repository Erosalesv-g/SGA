package com.sga.unemi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "estudiante_id", nullable = false)
    private Estudiante estudiante;

    @Column(nullable = false, length = 20)
    private String periodo;

    @Column(name = "fecha_matricula", nullable = false)
    private LocalDateTime fechaMatricula;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoMatricula estado;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    // Getters y Setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Estudiante getEstudiante() { return estudiante; }
    public void setEstudiante(Estudiante estudiante) { this.estudiante = estudiante; }

    public String getPeriodo() { return periodo; }
    public void setPeriodo(String periodo) { this.periodo = periodo; }

    public LocalDateTime getFechaMatricula() { return fechaMatricula; }
    public void setFechaMatricula(LocalDateTime fechaMatricula) { this.fechaMatricula = fechaMatricula; }

    public EstadoMatricula getEstado() { return estado; }
    public void setEstado(EstadoMatricula estado) { this.estado = estado; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}