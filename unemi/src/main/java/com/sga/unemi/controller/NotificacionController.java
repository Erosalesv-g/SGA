package com.sga.unemi.controller;

import com.sga.unemi.model.Notificacion;
import com.sga.unemi.service.NotificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notificaciones")
public class NotificacionController {

    private final NotificacionService notificacionService;

    public NotificacionController(NotificacionService notificacionService) {
        this.notificacionService = notificacionService;
    }

    @GetMapping("/usuario/{destinatarioId}")
    public ResponseEntity<List<Notificacion>> listarPorUsuario(@PathVariable UUID destinatarioId) {
        return ResponseEntity.ok(notificacionService.listarPorDestinatario(destinatarioId));
    }

    @PutMapping("/{id}/leida")
    public ResponseEntity<Notificacion> marcarComoLeida(@PathVariable UUID id) {
        return ResponseEntity.ok(notificacionService.marcarComoLeida(id));
    }
}