package com.sga.unemi.repository;

import com.sga.unemi.model.Horario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface HorarioRepository extends JpaRepository<Horario, UUID> {

    List<Horario> findByDocenteId(UUID docenteId);
    List<Horario> findByMateriaId(UUID materiaId);
}