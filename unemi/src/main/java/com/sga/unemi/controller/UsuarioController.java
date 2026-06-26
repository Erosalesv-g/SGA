package com.sga.unemi.controller;

import com.sga.unemi.dto.UsuarioRequest;
import com.sga.unemi.dto.UsuarioResponse;
import com.sga.unemi.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listar() {
        return ResponseEntity.ok(usuarioService.listarUsuarios());
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> crear(@RequestBody UsuarioRequest request, @RequestParam UUID actorId) {
        return ResponseEntity.ok(usuarioService.crearUsuario(request, actorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(usuarioService.obtenerUsuario(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizar(@PathVariable UUID id, @RequestBody UsuarioRequest request, @RequestParam UUID actorId) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, request, actorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id, @RequestParam UUID actorId) {
        usuarioService.desactivarUsuario(id, actorId);
        return ResponseEntity.noContent().build();
    }
}