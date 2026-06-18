package com.sga.unemi.repository;

import com.sga.unemi.model.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EstudianteRepository 
    extends JpaRepository<Estudiante, UUID> {

    Optional<Estudiante> findByCodigo(String codigo);
    boolean existsByCodigo(String codigo);
}