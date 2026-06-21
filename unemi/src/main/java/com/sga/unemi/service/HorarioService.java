package com.sga.unemi.service;

import com.sga.unemi.dto.HorarioRequest;
import com.sga.unemi.dto.HorarioResponse;
import com.sga.unemi.model.Docente;
import com.sga.unemi.model.Horario;
import com.sga.unemi.model.Materia;
import com.sga.unemi.repository.DocenteRepository;
import com.sga.unemi.repository.HorarioRepository;
import com.sga.unemi.repository.MateriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class HorarioService {

    private final HorarioRepository horarioRepository;
    private final DocenteRepository docenteRepository;
    private final MateriaRepository materiaRepository;

    public HorarioService(HorarioRepository horarioRepository,
                           DocenteRepository docenteRepository,
                           MateriaRepository materiaRepository) {
        this.horarioRepository = horarioRepository;
        this.docenteRepository = docenteRepository;
        this.materiaRepository = materiaRepository;
    }

    public List<HorarioResponse> listarTodos() {
        return horarioRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<HorarioResponse> listarPorDocente(UUID docenteId) {
        return horarioRepository.findByDocenteId(docenteId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public HorarioResponse crear(HorarioRequest request) {
        Docente docente = docenteRepository.findById(request.getDocenteId())
                .orElseThrow(() -> new RuntimeException("Docente no encontrado"));
        Materia materia = materiaRepository.findById(request.getMateriaId())
                .orElseThrow(() -> new RuntimeException("Materia no encontrada"));

        if (request.getHoraInicio().isAfter(request.getHoraFin())) {
            throw new RuntimeException("La hora de inicio debe ser antes que la hora de fin");
        }

        boolean hayConflicto = horarioRepository.findByDocenteId(request.getDocenteId()).stream()
                .filter(h -> h.getDiaSemana().equals(request.getDiaSemana()))
                .anyMatch(h -> request.getHoraInicio().isBefore(h.getHoraFin())
                        && request.getHoraFin().isAfter(h.getHoraInicio()));

        if (hayConflicto) {
            throw new RuntimeException("El docente ya tiene una clase asignada en ese día y horario");
        }

        Horario horario = new Horario();
        horario.setDocente(docente);
        horario.setMateria(materia);
        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setAula(request.getAula());
        horario.setPeriodo(request.getPeriodo());

        Horario guardado = horarioRepository.save(horario);
        return toResponse(guardado);
    }

    public HorarioResponse obtener(UUID id) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));
        return toResponse(horario);
    }

    public HorarioResponse actualizar(UUID id, HorarioRequest request) {
        Horario horario = horarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Horario no encontrado"));

        horario.setDiaSemana(request.getDiaSemana());
        horario.setHoraInicio(request.getHoraInicio());
        horario.setHoraFin(request.getHoraFin());
        horario.setAula(request.getAula());
        horario.setPeriodo(request.getPeriodo());

        Horario actualizado = horarioRepository.save(horario);
        return toResponse(actualizado);
    }

    public void eliminar(UUID id) {
        horarioRepository.deleteById(id);
    }

    private HorarioResponse toResponse(Horario h) {
        return new HorarioResponse(
                h.getId(), h.getDocente().getId(), h.getDocente().getNombre(),
                h.getMateria().getId(), h.getMateria().getNombre(),
                h.getDiaSemana(), h.getHoraInicio(), h.getHoraFin(),
                h.getAula(), h.getPeriodo()
        );
    }
}