package com.sga.unemi.service;

import com.sga.unemi.dto.EstudianteRequest;
import com.sga.unemi.dto.EstudianteResponse;
import com.sga.unemi.exception.ResourceNotFoundException;
import com.sga.unemi.exception.ValidationException;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.model.Rol;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.EstudianteRepository;
import com.sga.unemi.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lógica de negocio del módulo de estudiantes.
 * <p>
 * Usa la jerarquía de excepciones de negocio ({@link ResourceNotFoundException},
 * {@link ValidationException}) para que {@code GlobalExceptionHandler}
 * traduzca cada error a su código HTTP correcto (404, 400), en vez de un
 * 500 genérico o, como ocurría antes de la introducción de este manejador,
 * un 401 incorrecto para cualquier RuntimeException.
 */
@Service
public class EstudianteService {

    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public EstudianteService(EstudianteRepository estudianteRepository,
                              UsuarioRepository usuarioRepository,
                              PasswordEncoder passwordEncoder) {
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<EstudianteResponse> listarEstudiantes() {
        return estudianteRepository.findAll().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public EstudianteResponse crearEstudiante(EstudianteRequest request) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new ValidationException("El email ya está registrado");
        }
        if (estudianteRepository.existsByCodigo(request.getCodigo())) {
            throw new ValidationException("El código de estudiante ya existe");
        }

        Estudiante estudiante = new Estudiante();
        estudiante.setNombre(request.getNombre());
        estudiante.setEmail(request.getEmail());
        estudiante.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        estudiante.setRol(Rol.ESTUDIANTE);
        estudiante.setActivo(true);
        estudiante.setCodigo(request.getCodigo());
        estudiante.setNivel(request.getNivel());
        estudiante.setSeccion(request.getSeccion());
        asignarRepresentante(estudiante, request.getRepresentanteId());

        Estudiante guardado = estudianteRepository.save(estudiante);
        return toResponse(guardado);
    }

    public EstudianteResponse obtenerEstudiante(UUID id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        return toResponse(estudiante);
    }

    public EstudianteResponse actualizarEstudiante(UUID id, EstudianteRequest request) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));

        estudiante.setNombre(request.getNombre());
        estudiante.setNivel(request.getNivel());
        estudiante.setSeccion(request.getSeccion());
        asignarRepresentante(estudiante, request.getRepresentanteId());

        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            estudiante.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Estudiante actualizado = estudianteRepository.save(estudiante);
        return toResponse(actualizado);
    }

    public void desactivarEstudiante(UUID id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        estudiante.setActivo(false);
        estudianteRepository.save(estudiante);
    }

    public void activarEstudiante(UUID id) {
        Estudiante estudiante = estudianteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Estudiante no encontrado"));
        estudiante.setActivo(true);
        estudianteRepository.save(estudiante);
    }

    private void asignarRepresentante(Estudiante estudiante, UUID representanteId) {
        if (representanteId != null) {
            Usuario representante = usuarioRepository.findById(representanteId)
                    .orElseThrow(() -> new ResourceNotFoundException("Representante no encontrado"));
            estudiante.setRepresentante(representante);
        } else {
            estudiante.setRepresentante(null);
        }
    }

    private EstudianteResponse toResponse(Estudiante e) {
        Usuario rep = e.getRepresentante();
        return new EstudianteResponse(
                e.getId(), e.getNombre(), e.getEmail(),
                e.getCodigo(), e.getNivel(), e.getSeccion(), e.isActivo(),
                rep != null ? rep.getId() : null,
                rep != null ? rep.getNombre() : null
        );
    }
}