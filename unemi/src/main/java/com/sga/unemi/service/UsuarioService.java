package com.sga.unemi.service;

import com.sga.unemi.dto.UsuarioRequest;
import com.sga.unemi.dto.UsuarioResponse;
import com.sga.unemi.model.Rol;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaLogService auditoriaLogService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder,
                           AuditoriaLogService auditoriaLogService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaLogService = auditoriaLogService;
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioResponse(u.getId(), u.getNombre(), u.getEmail(), u.getRol().name(), u.isActivo()))
                .collect(Collectors.toList());
    }

    public UsuarioResponse crearUsuario(UsuarioRequest request, UUID actorId) {
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("El email ya está registrado");
        }
        Usuario usuario = new Usuario();
        usuario.setNombre(request.getNombre());
        usuario.setEmail(request.getEmail());
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(Rol.valueOf(request.getRol()));
        usuario.setActivo(true);
        Usuario guardado = usuarioRepository.save(usuario);

        Usuario actor = usuarioRepository.findById(actorId).orElse(guardado);
        auditoriaLogService.registrar(actor, "CREAR", "Usuario", guardado.getId(),
                "Creó el usuario " + guardado.getNombre() + " (" + guardado.getEmail() + ") con rol " + guardado.getRol());

        return new UsuarioResponse(guardado.getId(), guardado.getNombre(), guardado.getEmail(), guardado.getRol().name(), guardado.isActivo());
    }

    public UsuarioResponse obtenerUsuario(UUID id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return new UsuarioResponse(usuario.getId(), usuario.getNombre(), usuario.getEmail(), usuario.getRol().name(), usuario.isActivo());
    }

    public UsuarioResponse actualizarUsuario(UUID id, UsuarioRequest request, UUID actorId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        String rolAnterior = usuario.getRol().name();
        usuario.setNombre(request.getNombre());
        usuario.setRol(Rol.valueOf(request.getRol()));
        if (request.getPassword() != null && !request.getPassword().isEmpty()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        Usuario actualizado = usuarioRepository.save(usuario);

        Usuario actor = usuarioRepository.findById(actorId).orElse(actualizado);
        auditoriaLogService.registrar(actor, "EDITAR", "Usuario", actualizado.getId(),
                "Editó al usuario " + actualizado.getNombre() + " (rol " + rolAnterior + " → " + actualizado.getRol() + ")");

        return new UsuarioResponse(actualizado.getId(), actualizado.getNombre(), actualizado.getEmail(), actualizado.getRol().name(), actualizado.isActivo());
    }

    public void desactivarUsuario(UUID id, UUID actorId) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        Usuario actor = usuarioRepository.findById(actorId).orElse(usuario);
        auditoriaLogService.registrar(actor, "ELIMINAR", "Usuario", usuario.getId(),
                "Desactivó al usuario " + usuario.getNombre());
    }
}