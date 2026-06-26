package com.sga.unemi.service;

import com.sga.unemi.dto.AsistenciaRequest;
import com.sga.unemi.dto.AsistenciaResponse;
import com.sga.unemi.model.Asistencia;
import com.sga.unemi.model.EstadoAsist;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.model.Materia;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.AsistenciaRepository;
import com.sga.unemi.repository.EstudianteRepository;
import com.sga.unemi.repository.MateriaRepository;
import com.sga.unemi.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final EstudianteRepository estudianteRepository;
    private final MateriaRepository materiaRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaLogService auditoriaLogService;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                              EstudianteRepository estudianteRepository,
                              MateriaRepository materiaRepository,
                              UsuarioRepository usuarioRepository,
                              AuditoriaLogService auditoriaLogService) {
        this.asistenciaRepository = asistenciaRepository;
        this.estudianteRepository = estudianteRepository;
        this.materiaRepository = materiaRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaLogService = auditoriaLogService;
    }

    public List<AsistenciaResponse> listarTodas() {
        return asistenciaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<AsistenciaResponse> listarPorEstudiante(UUID estudianteId) {
        return asistenciaRepository.findByEstudianteId(estudianteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public AsistenciaResponse crear(AsistenciaRequest request, UUID actorId) {
        Estudiante estudiante = estudianteRepository.findById(request.getEstudianteId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        Materia materia = materiaRepository.findById(request.getMateriaId())
                .orElseThrow(() -> new RuntimeException("Materia no encontrada"));

        Asistencia asistencia = new Asistencia();
        asistencia.setFecha(request.getFecha());
        asistencia.setEstado(EstadoAsist.valueOf(request.getEstado()));
        asistencia.setEstudiante(estudiante);
        asistencia.setMateria(materia);

        Asistencia guardada = asistenciaRepository.save(asistencia);

        Usuario actor = usuarioRepository.findById(actorId).orElse(null);
        if (actor != null) {
            auditoriaLogService.registrar(actor, "CREAR", "Asistencia", guardada.getId(),
                    "Registró asistencia de " + estudiante.getNombre() + " en " + materia.getNombre() +
                            " (" + guardada.getEstado() + ") el " + guardada.getFecha());
        }

        return toResponse(guardada);
    }

    public AsistenciaResponse obtener(UUID id) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));
        return toResponse(asistencia);
    }

    public AsistenciaResponse justificar(UUID id, UUID actorId) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));

        if (!asistencia.justificar()) {
            throw new RuntimeException("Solo se pueden justificar asistencias marcadas como ausente");
        }

        Asistencia actualizada = asistenciaRepository.save(asistencia);

        Usuario actor = usuarioRepository.findById(actorId).orElse(null);
        if (actor != null) {
            auditoriaLogService.registrar(actor, "EDITAR", "Asistencia", actualizada.getId(),
                    "Justificó la asistencia de " + actualizada.getEstudiante().getNombre() +
                            " en " + actualizada.getMateria().getNombre() + " del " + actualizada.getFecha());
        }

        return toResponse(actualizada);
    }

    public void eliminar(UUID id, UUID actorId) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));

        Usuario actor = usuarioRepository.findById(actorId).orElse(null);
        if (actor != null) {
            auditoriaLogService.registrar(actor, "ELIMINAR", "Asistencia", asistencia.getId(),
                    "Eliminó el registro de asistencia de " + asistencia.getEstudiante().getNombre() +
                            " en " + asistencia.getMateria().getNombre() + " del " + asistencia.getFecha());
        }

        asistenciaRepository.deleteById(id);
    }

    private AsistenciaResponse toResponse(Asistencia a) {
        return new AsistenciaResponse(
                a.getId(), a.getFecha(), a.getEstado().name(),
                a.getEstudiante().getId(), a.getEstudiante().getNombre(),
                a.getMateria().getId(), a.getMateria().getNombre()
        );
    }
}