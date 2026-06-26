package com.sga.unemi.service;

import com.sga.unemi.dto.AuditoriaLogResponse;
import com.sga.unemi.model.AuditoriaLog;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.AuditoriaLogRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AuditoriaLogService {

    private final AuditoriaLogRepository auditoriaLogRepository;

    public AuditoriaLogService(AuditoriaLogRepository auditoriaLogRepository) {
        this.auditoriaLogRepository = auditoriaLogRepository;
    }

    public List<AuditoriaLogResponse> listarTodos() {
        return auditoriaLogRepository.findAllByOrderByFechaDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public void registrar(Usuario usuario, String accion, String entidad, UUID entidadId, String descripcion) {
        AuditoriaLog log = new AuditoriaLog();
        log.setUsuario(usuario);
        log.setUsuarioNombre(usuario.getNombre());
        log.setAccion(accion);
        log.setEntidad(entidad);
        log.setEntidadId(entidadId);
        log.setDescripcion(descripcion);
        auditoriaLogRepository.save(log);
    }

    private AuditoriaLogResponse toResponse(AuditoriaLog log) {
        return new AuditoriaLogResponse(
                log.getId(), log.getUsuarioNombre(), log.getAccion(), log.getEntidad(),
                log.getEntidadId(), log.getDescripcion(), log.getFecha()
        );
    }
}