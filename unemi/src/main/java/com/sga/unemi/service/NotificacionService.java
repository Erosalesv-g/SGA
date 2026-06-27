package com.sga.unemi.service;

import com.sga.unemi.model.Notificacion;
import com.sga.unemi.repository.NotificacionRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;

    public NotificacionService(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    public List<Notificacion> listarPorDestinatario(UUID destinatarioId) {
        return notificacionRepository.findByDestinatarioIdOrderByFechaCreacionDesc(destinatarioId);
    }

    public Notificacion marcarComoLeida(UUID id) {
        Notificacion notificacion = notificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        notificacion.setLeida(true);
        return notificacionRepository.save(notificacion);
    }
}