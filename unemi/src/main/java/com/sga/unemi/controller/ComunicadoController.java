package com.sga.unemi.controller;

import com.sga.unemi.dto.ComunicadoRequest;
import com.sga.unemi.dto.ComunicadoResponse;
import com.sga.unemi.service.ComunicadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/comunicados")
public class ComunicadoController {

    private final ComunicadoService comunicadoService;

    public ComunicadoController(ComunicadoService comunicadoService) {
        this.comunicadoService = comunicadoService;
    }

    @GetMapping
    public ResponseEntity<List<ComunicadoResponse>> listar() {
        return ResponseEntity.ok(comunicadoService.listarTodos());
    }

    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<ComunicadoResponse>> listarPorRol(@PathVariable String rol) {
        return ResponseEntity.ok(comunicadoService.listarPorRol(rol));
    }

    @PostMapping
    public ResponseEntity<ComunicadoResponse> crear(@RequestBody ComunicadoRequest request) {
        return ResponseEntity.ok(comunicadoService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ComunicadoResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(comunicadoService.obtener(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        comunicadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}