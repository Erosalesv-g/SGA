package com.sga.unemi.service;

import com.sga.unemi.dto.MateriaRequest;
import com.sga.unemi.dto.MateriaResponse;
import com.sga.unemi.model.Docente;
import com.sga.unemi.model.Materia;
import com.sga.unemi.repository.DocenteRepository;
import com.sga.unemi.repository.MateriaRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class MateriaService {

    private final MateriaRepository materiaRepository;
    private final DocenteRepository docenteRepository;

    public MateriaService(MateriaRepository materiaRepository, DocenteRepository docenteRepository) {
        this.materiaRepository = materiaRepository;
        this.docenteRepository = docenteRepository;
    }

    public List<MateriaResponse> listarMaterias() {
        return materiaRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public MateriaResponse obtenerMateria(UUID id) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada"));
        return toResponse(materia);
    }

    public MateriaResponse crearMateria(MateriaRequest request) {
        Materia materia = new Materia();
        materia.setNombre(request.getNombre());
        materia.setCodigo(request.getCodigo());
        materia.setCreditos(request.getCreditos());
        asignarDocente(materia, request.getDocenteId());

        Materia guardada = materiaRepository.save(materia);
        return toResponse(guardada);
    }

    public MateriaResponse actualizarMateria(UUID id, MateriaRequest request) {
        Materia materia = materiaRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Materia no encontrada"));

        materia.setNombre(request.getNombre());
        materia.setCodigo(request.getCodigo());
        materia.setCreditos(request.getCreditos());
        asignarDocente(materia, request.getDocenteId());

        Materia actualizada = materiaRepository.save(materia);
        return toResponse(actualizada);
    }

    public void eliminarMateria(UUID id) {
        materiaRepository.deleteById(id);
    }

    private void asignarDocente(Materia materia, UUID docenteId) {
        if (docenteId != null) {
            Docente docente = docenteRepository.findById(docenteId)
                    .orElseThrow(() -> new NoSuchElementException("Docente no encontrado"));
            materia.setDocente(docente);
        } else {
            materia.setDocente(null);
        }
    }

    private MateriaResponse toResponse(Materia materia) {
        return new MateriaResponse(
                materia.getId(),
                materia.getNombre(),
                materia.getCodigo(),
                materia.getCreditos(),
                materia.getDocente() != null ? materia.getDocente().getId() : null,
                materia.getDocente() != null ? materia.getDocente().getNombre() : null
        );
    }
}