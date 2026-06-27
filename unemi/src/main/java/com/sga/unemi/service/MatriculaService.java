package com.sga.unemi.service;

import com.sga.unemi.dto.MatriculaRequest;
import com.sga.unemi.dto.MatriculaResponse;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.model.EstadoMatricula;
import com.sga.unemi.model.Matricula;
import com.sga.unemi.repository.EstudianteRepository;
import com.sga.unemi.repository.MatriculaRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class MatriculaService {

    private final MatriculaRepository matriculaRepository;
    private final EstudianteRepository estudianteRepository;

    public MatriculaService(MatriculaRepository matriculaRepository,
                             EstudianteRepository estudianteRepository) {
        this.matriculaRepository = matriculaRepository;
        this.estudianteRepository = estudianteRepository;
    }

    public List<MatriculaResponse> listarMatriculas() {
        return matriculaRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<MatriculaResponse> listarPorEstudiante(UUID estudianteId) {
        return matriculaRepository.findByEstudianteId(estudianteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public MatriculaResponse registrar(MatriculaRequest request) {
        if (matriculaRepository.existsByEstudianteIdAndPeriodo(request.getEstudianteId(), request.getPeriodo())) {
            throw new RuntimeException("El estudiante ya tiene una matrícula registrada en este periodo");
        }

        Estudiante estudiante = estudianteRepository.findById(request.getEstudianteId())
                .orElseThrow(() -> new RuntimeException("Estudiante no encontrado"));

        Matricula matricula = new Matricula();
        matricula.setEstudiante(estudiante);
        matricula.setPeriodo(request.getPeriodo());
        matricula.setFechaMatricula(LocalDateTime.now());
        matricula.setEstado(EstadoMatricula.ACTIVA);
        matricula.setObservaciones(request.getObservaciones());

        Matricula guardada = matriculaRepository.save(matricula);
        return toResponse(guardada);
    }

    public MatriculaResponse obtener(UUID id) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));
        return toResponse(matricula);
    }

    public MatriculaResponse actualizar(UUID id, MatriculaRequest request) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));

        matricula.setPeriodo(request.getPeriodo());
        matricula.setObservaciones(request.getObservaciones());

        Matricula actualizada = matriculaRepository.save(matricula);
        return toResponse(actualizada);
    }

    public void anular(UUID id) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));
        matricula.setEstado(EstadoMatricula.ANULADA);
        matriculaRepository.save(matricula);
    }

    public void reactivar(UUID id) {
        Matricula matricula = matriculaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Matrícula no encontrada"));
        matricula.setEstado(EstadoMatricula.ACTIVA);
        matriculaRepository.save(matricula);
    }

    private MatriculaResponse toResponse(Matricula m) {
        return new MatriculaResponse(
                m.getId(),
                m.getEstudiante().getId(),
                m.getEstudiante().getNombre(),
                m.getPeriodo(),
                m.getFechaMatricula(),
                m.getEstado(),
                m.getObservaciones()
        );
    }
}