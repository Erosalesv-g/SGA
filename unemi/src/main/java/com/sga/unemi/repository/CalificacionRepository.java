package com.sga.unemi.repository;

import com.sga.unemi.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CalificacionRepository 
    extends JpaRepository<Calificacion, UUID> {

    List<Calificacion> findByEstudianteId(UUID estudianteId);
    List<Calificacion> findByMateriaId(UUID materiaId);
    List<Calificacion> findByEstudianteIdAndMateriaId(
        UUID estudianteId, UUID materiaId);

    @Query("SELECT c FROM Calificacion c " +
           "JOIN FETCH c.estudiante " +
           "JOIN FETCH c.materia " +
           "JOIN FETCH c.docente")
    List<Calificacion> findAllConDetalles();
}