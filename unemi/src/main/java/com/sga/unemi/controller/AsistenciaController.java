package com.sga.unemi.controller;

import com.sga.unemi.dto.AsistenciaRequest;
import com.sga.unemi.dto.AsistenciaResponse;
import com.sga.unemi.service.AsistenciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping
    public ResponseEntity<List<AsistenciaResponse>> listar() {
        return ResponseEntity.ok(asistenciaService.listarTodas());
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<AsistenciaResponse>> listarPorEstudiante(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(asistenciaService.listarPorEstudiante(estudianteId));
    }

    @PostMapping
    public ResponseEntity<AsistenciaResponse> crear(@RequestBody AsistenciaRequest request, @RequestParam UUID actorId) {
        return ResponseEntity.ok(asistenciaService.crear(request, actorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsistenciaResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(asistenciaService.obtener(id));
    }

    @PatchMapping("/{id}/justificar")
    public ResponseEntity<AsistenciaResponse> justificar(@PathVariable UUID id, @RequestParam UUID actorId) {
        return ResponseEntity.ok(asistenciaService.justificar(id, actorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id, @RequestParam UUID actorId) {
        asistenciaService.eliminar(id, actorId);
        return ResponseEntity.noContent().build();
    }
}