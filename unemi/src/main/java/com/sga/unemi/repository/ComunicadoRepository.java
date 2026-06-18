package com.sga.unemi.repository;

import com.sga.unemi.model.Comunicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ComunicadoRepository extends JpaRepository<Comunicado, UUID> {

    List<Comunicado> findByDestinatarioRol(String destinatarioRol);
    List<Comunicado> findAllByOrderByFechaEnvioDesc();
}