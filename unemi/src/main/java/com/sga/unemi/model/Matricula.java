package com.sga.unemi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de matrícula de un estudiante en un período académico (RF-02).
 * <p>
 * Implementa bloqueo optimista mediante el campo {@code version} anotado
 * con {@code @Version}: si dos usuarios intentan modificar la misma
 * matrícula de forma simultánea, el segundo recibirá una excepción de
 * concurrencia ({@code OptimisticLockingFailureException}) en vez de que
 * sus cambios sobrescriban silenciosamente los del primero. Esto garantiza
 * la integridad de los datos de matrícula en escenarios de acceso
 * concurrente (por ejemplo, dos secretarias procesando matrículas al mismo
 * tiempo).
 */
@Entity
@Table(name = "matriculas")
public class Matricula {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Número de versión del registro, gestionado automáticamente por JPA.
     * Se incrementa en cada actualización; si dos transacciones leen la
     * misma versión y ambas intentan actualizar, la segunda falla con
     * {@code OptimisticLockingFailureException}.
     */
    @Version
    @Column(nullable = false)
    private Long version;

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

    public Long getVersion() { return version; }
    public void setVersion(Long version) { this.version = version; }

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