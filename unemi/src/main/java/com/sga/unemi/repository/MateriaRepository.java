package com.sga.unemi.repository;

import com.sga.unemi.model.Materia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MateriaRepository 
    extends JpaRepository<Materia, UUID> {

    Optional<Materia> findByCodigo(String codigo);
    List<Materia> findByDocenteId(UUID docenteId);
    boolean existsByCodigo(String codigo);
}