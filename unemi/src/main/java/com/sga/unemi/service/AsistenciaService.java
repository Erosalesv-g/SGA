package com.sga.unemi.service;

import com.sga.unemi.dto.AsistenciaRequest;
import com.sga.unemi.dto.AsistenciaResponse;
import com.sga.unemi.model.Asistencia;
import com.sga.unemi.model.EstadoAsist;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.model.Materia;
import com.sga.unemi.repository.AsistenciaRepository;
import com.sga.unemi.repository.EstudianteRepository;
import com.sga.unemi.repository.MateriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class AsistenciaService {

    private final AsistenciaRepository asistenciaRepository;
    private final EstudianteRepository estudianteRepository;
    private final MateriaRepository materiaRepository;

    public AsistenciaService(AsistenciaRepository asistenciaRepository,
                              EstudianteRepository estudianteRepository,
                              MateriaRepository materiaRepository) {
        this.asistenciaRepository = asistenciaRepository;
        this.estudianteRepository = estudianteRepository;
        this.materiaRepository = materiaRepository;
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

    public AsistenciaResponse crear(AsistenciaRequest request) {
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
        return toResponse(guardada);
    }

    public AsistenciaResponse obtener(UUID id) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));
        return toResponse(asistencia);
    }

    public AsistenciaResponse justificar(UUID id) {
        Asistencia asistencia = asistenciaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asistencia no encontrada"));

        if (!asistencia.justificar()) {
            throw new RuntimeException("Solo se pueden justificar asistencias marcadas como ausente");
        }

        Asistencia actualizada = asistenciaRepository.save(asistencia);
        return toResponse(actualizada);
    }

    public void eliminar(UUID id) {
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