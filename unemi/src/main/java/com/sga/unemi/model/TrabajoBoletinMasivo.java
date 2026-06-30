package com.sga.unemi.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Registro de seguimiento de un trabajo de generación masiva de boletines
 * (RNF-0011: Escalabilidad de Usuarios).
 * <p>
 * Cuando el RECTOR solicita generar los boletines de todos los estudiantes
 * de un nivel, se crea un registro de este tipo en estado
 * {@link EstadoTrabajo#PENDIENTE} y se publica un evento a RabbitMQ. El
 * consumidor ({@code BoletinMasivoConsumer}) actualiza este mismo registro
 * a medida que procesa cada boletín, permitiendo que el frontend consulte
 * el progreso sin tener que esperar a que termine todo el lote.
 */
@Entity
@Table(name = "trabajos_boletin_masivo")
public class TrabajoBoletinMasivo {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 50)
    private String nivel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoTrabajo estado;

    @Column(name = "total_estudiantes", nullable = false)
    private int totalEstudiantes;

    @Column(nullable = false)
    private int procesados;

    @Column(nullable = false)
    private int fallidos;

    @ManyToOne
    @JoinColumn(name = "solicitado_por", nullable = false)
    private Usuario solicitadoPor;

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDateTime fechaInicio;

    @Column(name = "fecha_fin")
    private LocalDateTime fechaFin;

    @Column(name = "mensaje_error", columnDefinition = "TEXT")
    private String mensajeError;

    @PrePersist
    public void prePersist() {
        if (fechaInicio == null) {
            fechaInicio = LocalDateTime.now();
        }
        if (estado == null) {
            estado = EstadoTrabajo.PENDIENTE;
        }
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public EstadoTrabajo getEstado() { return estado; }
    public void setEstado(EstadoTrabajo estado) { this.estado = estado; }

    public int getTotalEstudiantes() { return totalEstudiantes; }
    public void setTotalEstudiantes(int totalEstudiantes) { this.totalEstudiantes = totalEstudiantes; }

    public int getProcesados() { return procesados; }
    public void setProcesados(int procesados) { this.procesados = procesados; }

    public int getFallidos() { return fallidos; }
    public void setFallidos(int fallidos) { this.fallidos = fallidos; }

    public Usuario getSolicitadoPor() { return solicitadoPor; }
    public void setSolicitadoPor(Usuario solicitadoPor) { this.solicitadoPor = solicitadoPor; }

    public LocalDateTime getFechaInicio() { return fechaInicio; }
    public void setFechaInicio(LocalDateTime fechaInicio) { this.fechaInicio = fechaInicio; }

    public LocalDateTime getFechaFin() { return fechaFin; }
    public void setFechaFin(LocalDateTime fechaFin) { this.fechaFin = fechaFin; }

    public String getMensajeError() { return mensajeError; }
    public void setMensajeError(String mensajeError) { this.mensajeError = mensajeError; }
}