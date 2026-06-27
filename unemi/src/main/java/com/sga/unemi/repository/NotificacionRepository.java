package com.sga.unemi.repository;

import com.sga.unemi.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {

    List<Notificacion> findByDestinatarioIdOrderByFechaCreacionDesc(UUID destinatarioId);
}