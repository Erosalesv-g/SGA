package com.sga.unemi.repository;

import com.sga.unemi.model.TrabajoBoletinMasivo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Acceso a datos de los trabajos de generación masiva de boletines.
 *
 * @see TrabajoBoletinMasivo
 */
@Repository
public interface TrabajoBoletinMasivoRepository extends JpaRepository<TrabajoBoletinMasivo, UUID> {
}