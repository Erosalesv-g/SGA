package com.sga.unemi.repository;

import com.sga.unemi.model.AuditoriaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AuditoriaLogRepository extends JpaRepository<AuditoriaLog, java.util.UUID> {
    List<AuditoriaLog> findAllByOrderByFechaDesc();
}