package com.sga.unemi.controller;

import com.sga.unemi.dto.DocenteRequest;
import com.sga.unemi.dto.DocenteResponse;
import com.sga.unemi.service.DocenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    public ResponseEntity<List<DocenteResponse>> listar() {
        return ResponseEntity.ok(docenteService.listarDocentes());
    }

    @PostMapping
    public ResponseEntity<DocenteResponse> crear(@RequestBody DocenteRequest request) {
        return ResponseEntity.ok(docenteService.crearDocente(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DocenteResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(docenteService.obtenerDocente(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DocenteResponse> actualizar(@PathVariable UUID id, @RequestBody DocenteRequest request) {
        return ResponseEntity.ok(docenteService.actualizarDocente(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id) {
        docenteService.desactivarDocente(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/activar")
    public ResponseEntity<Void> activar(@PathVariable UUID id) {
        docenteService.activarDocente(id);
        return ResponseEntity.noContent().build();
    }
}