package com.sga.unemi.service;

import com.sga.unemi.dto.DocenteRequest;
import com.sga.unemi.dto.DocenteResponse;
import com.sga.unemi.model.Docente;
import com.sga.unemi.model.Rol;
import com.sga.unemi.repository.DocenteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
public class DocenteService {

    private final DocenteRepository docenteRepository;
    private final PasswordEncoder passwordEncoder;

    public DocenteService(DocenteRepository docenteRepository, PasswordEncoder passwordEncoder) {
        this.docenteRepository = docenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<DocenteResponse> listarDocentes() {
        return docenteRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public DocenteResponse obtenerDocente(UUID id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Docente no encontrado"));
        return toResponse(docente);
    }

    public DocenteResponse crearDocente(DocenteRequest request) {
        Docente docente = new Docente();
        docente.setNombre(request.getNombre());
        docente.setEmail(request.getEmail());
        docente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        docente.setRol(Rol.DOCENTE);
        docente.setActivo(true);
        docente.setCedula(request.getCedula());
        docente.setTitulo(request.getTitulo());
        docente.setEspecialidad(request.getEspecialidad());

        Docente guardado = docenteRepository.save(docente);
        return toResponse(guardado);
    }

    public DocenteResponse actualizarDocente(UUID id, DocenteRequest request) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Docente no encontrado"));

        docente.setNombre(request.getNombre());
        docente.setEmail(request.getEmail());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            docente.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        docente.setCedula(request.getCedula());
        docente.setTitulo(request.getTitulo());
        docente.setEspecialidad(request.getEspecialidad());

        Docente actualizado = docenteRepository.save(docente);
        return toResponse(actualizado);
    }

    public void desactivarDocente(UUID id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Docente no encontrado"));
        docente.setActivo(false);
        docenteRepository.save(docente);
    }

    public void activarDocente(UUID id) {
        Docente docente = docenteRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Docente no encontrado"));
        docente.setActivo(true);
        docenteRepository.save(docente);
    }

    private DocenteResponse toResponse(Docente docente) {
        return new DocenteResponse(
                docente.getId(),
                docente.getNombre(),
                docente.getEmail(),
                docente.getCedula(),
                docente.getTitulo(),
                docente.getEspecialidad(),
                docente.isActivo()
        );
    }
}