package com.sga.unemi.dto;
 
import java.util.UUID;
 
public class ComunicadoMensaje {
 
    private UUID comunicadoId;
 
    public ComunicadoMensaje() {
    }
 
    public ComunicadoMensaje(UUID comunicadoId) {
        this.comunicadoId = comunicadoId;
    }
 
    public UUID getComunicadoId() { return comunicadoId; }
    public void setComunicadoId(UUID comunicadoId) { this.comunicadoId = comunicadoId; }
}