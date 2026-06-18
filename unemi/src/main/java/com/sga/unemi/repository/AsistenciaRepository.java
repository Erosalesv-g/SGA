package com.sga.unemi.repository;

import com.sga.unemi.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface AsistenciaRepository 
    extends JpaRepository<Asistencia, UUID> {

    List<Asistencia> findByEstudianteId(UUID estudianteId);
    List<Asistencia> findByMateriaId(UUID materiaId);
    List<Asistencia> findByEstudianteIdAndFecha(
        UUID estudianteId, LocalDate fecha);
}