package com.sga.unemi.service;

import com.sga.unemi.dto.CalificacionRequest;
import com.sga.unemi.dto.CalificacionResponse;
import com.sga.unemi.model.Calificacion;
import com.sga.unemi.model.Docente;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.model.Materia;
import com.sga.unemi.model.TipoCalif;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.CalificacionRepository;
import com.sga.unemi.repository.DocenteRepository;
import com.sga.unemi.repository.EstudianteRepository;
import com.sga.unemi.repository.MateriaRepository;
import com.sga.unemi.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final EstudianteRepository estudianteRepository;
    private final MateriaRepository materiaRepository;
    private final DocenteRepository docenteRepository;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaLogService auditoriaLogService;

    public CalificacionService(CalificacionRepository calificacionRepository,
                                EstudianteRepository estudianteRepository,
                                MateriaRepository materiaRepository,
                                DocenteRepository docenteRepository,
                                UsuarioRepository usuarioRepository,
                                AuditoriaLogService auditoriaLogService) {
        this.calificacionRepository = calificacionRepository;
        this.estudianteRepository = estudianteRepository;
        this.materiaRepository = materiaRepository;
        this.docenteRepository = docenteRepository;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaLogService = auditoriaLogService;
    }

    public List<CalificacionResponse> listarTodas() {
        return calificacionRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CalificacionResponse> listarPorEstudiante(UUID estudianteId) {
        return calificacionRepository.findByEstudianteId(estudianteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CalificacionResponse crear(CalificacionRequest request, UUID actorId) {
        Estudiante estudiante = estudianteRepository.findById(request.getEstudianteId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));
        Materia materia = materiaRepository.findById(request.getMateriaId())
                .orElseThrow(() -> new RuntimeException("Materia no encontrada"));
        Docente docente = docenteRepository.findById(request.getDocenteId())
                .orElseThrow(() -> new RuntimeException("Docente no encontrado"));

        Calificacion calificacion = new Calificacion();
        calificacion.setValor(request.getValor());
        calificacion.setTipo(TipoCalif.valueOf(request.getTipo()));
        calificacion.setFechaRegistro(request.getFechaRegistro());
        calificacion.setEstudiante(estudiante);
        calificacion.setMateria(materia);
        calificacion.setDocente(docente);

        if (!calificacion.validarRango()) {
            throw new RuntimeException("La calificación debe estar entre 0 y 10");
        }

        Calificacion guardada = calificacionRepository.save(calificacion);

        Usuario actor = usuarioRepository.findById(actorId).orElse(null);
        if (actor != null) {
            auditoriaLogService.registrar(actor, "CREAR", "Calificacion", guardada.getId(),
                    "Registró calificación de " + estudiante.getNombre() + " en " + materia.getNombre() +
                            ": " + guardada.getValor() + " (" + guardada.getTipo() + ")");
        }

        return toResponse(guardada);
    }

    public CalificacionResponse obtener(UUID id) {
        Calificacion calificacion = calificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada"));
        return toResponse(calificacion);
    }

    public CalificacionResponse actualizar(UUID id, CalificacionRequest request, UUID actorId) {
        Calificacion calificacion = calificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada"));

        Double valorAnterior = calificacion.getValor();

        calificacion.setValor(request.getValor());
        calificacion.setTipo(TipoCalif.valueOf(request.getTipo()));
        calificacion.setFechaRegistro(request.getFechaRegistro());

        if (!calificacion.validarRango()) {
            throw new RuntimeException("La calificación debe estar entre 0 y 10");
        }

        Calificacion actualizada = calificacionRepository.save(calificacion);

        Usuario actor = usuarioRepository.findById(actorId).orElse(null);
        if (actor != null) {
            auditoriaLogService.registrar(actor, "EDITAR", "Calificacion", actualizada.getId(),
                    "Editó la calificación de " + actualizada.getEstudiante().getNombre() +
                            " en " + actualizada.getMateria().getNombre() +
                            " de " + valorAnterior + " a " + actualizada.getValor());
        }

        return toResponse(actualizada);
    }

    public void eliminar(UUID id, UUID actorId) {
        Calificacion calificacion = calificacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Calificación no encontrada"));

        Usuario actor = usuarioRepository.findById(actorId).orElse(null);
        if (actor != null) {
            auditoriaLogService.registrar(actor, "ELIMINAR", "Calificacion", calificacion.getId(),
                    "Eliminó la calificación de " + calificacion.getEstudiante().getNombre() +
                            " en " + calificacion.getMateria().getNombre() + " (valor: " + calificacion.getValor() + ")");
        }

        calificacionRepository.deleteById(id);
    }

    private CalificacionResponse toResponse(Calificacion c) {
        return new CalificacionResponse(
                c.getId(), c.getValor(), c.getTipo().name(), c.getFechaRegistro(),
                c.getEstudiante().getId(), c.getEstudiante().getNombre(),
                c.getMateria().getId(), c.getMateria().getNombre(),
                c.getDocente().getId(), c.getDocente().getNombre()
        );
    }
}