package com.sga.unemi.dto;

import java.util.UUID;

/**
 * Mensaje publicado a RabbitMQ para disparar el procesamiento asíncrono de
 * un trabajo de generación masiva de boletines.
 */
public class BoletinMasivoMensaje {

    private UUID trabajoId;

    public BoletinMasivoMensaje() {
    }

    public BoletinMasivoMensaje(UUID trabajoId) {
        this.trabajoId = trabajoId;
    }

    public UUID getTrabajoId() { return trabajoId; }
    public void setTrabajoId(UUID trabajoId) { this.trabajoId = trabajoId; }
}