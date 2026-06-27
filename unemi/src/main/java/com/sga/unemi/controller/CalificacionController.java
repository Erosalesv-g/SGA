package com.sga.unemi.controller;

import com.sga.unemi.dto.CalificacionRequest;
import com.sga.unemi.dto.CalificacionResponse;
import com.sga.unemi.service.CalificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @GetMapping
    public ResponseEntity<List<CalificacionResponse>> listar() {
        return ResponseEntity.ok(calificacionService.listarTodas());
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<CalificacionResponse>> listarPorEstudiante(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(calificacionService.listarPorEstudiante(estudianteId));
    }

    @PostMapping
    public ResponseEntity<CalificacionResponse> crear(@RequestBody CalificacionRequest request, @RequestParam UUID actorId) {
        return ResponseEntity.ok(calificacionService.crear(request, actorId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CalificacionResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(calificacionService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CalificacionResponse> actualizar(@PathVariable UUID id, @RequestBody CalificacionRequest request, @RequestParam UUID actorId) {
        return ResponseEntity.ok(calificacionService.actualizar(id, request, actorId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id, @RequestParam UUID actorId) {
        calificacionService.eliminar(id, actorId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/promedio/{estudianteId}/{materiaId}")
    public ResponseEntity<Double> obtenerPromedio(@PathVariable UUID estudianteId, @PathVariable UUID materiaId) {
        return ResponseEntity.ok(calificacionService.calcularPromedio(estudianteId, materiaId));
    }
}