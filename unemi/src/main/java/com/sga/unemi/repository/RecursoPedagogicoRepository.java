package com.sga.unemi.repository;

import com.sga.unemi.model.RecursoPedagogico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface RecursoPedagogicoRepository extends JpaRepository<RecursoPedagogico, UUID> {
    List<RecursoPedagogico> findByMateriaId(UUID materiaId);
    List<RecursoPedagogico> findByDocenteId(UUID docenteId);
}