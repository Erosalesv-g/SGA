package com.sga.unemi.repository;

import com.sga.unemi.model.Matricula;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MatriculaRepository extends JpaRepository<Matricula, UUID> {

    List<Matricula> findByEstudianteId(UUID estudianteId);

    boolean existsByEstudianteIdAndPeriodo(UUID estudianteId, String periodo);
}