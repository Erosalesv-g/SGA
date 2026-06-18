package com.sga.unemi.controller;

import com.sga.unemi.dto.EstudianteRequest;
import com.sga.unemi.dto.EstudianteResponse;
import com.sga.unemi.service.EstudianteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/estudiantes")
public class EstudianteController {

    private final EstudianteService estudianteService;

    public EstudianteController(EstudianteService estudianteService) {
        this.estudianteService = estudianteService;
    }

    @GetMapping
    public ResponseEntity<List<EstudianteResponse>> listar() {
        return ResponseEntity.ok(estudianteService.listarEstudiantes());
    }

    @PostMapping
    public ResponseEntity<EstudianteResponse> crear(@RequestBody EstudianteRequest request) {
        return ResponseEntity.ok(estudianteService.crearEstudiante(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<EstudianteResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(estudianteService.obtenerEstudiante(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EstudianteResponse> actualizar(@PathVariable UUID id, @RequestBody EstudianteRequest request) {
        return ResponseEntity.ok(estudianteService.actualizarEstudiante(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> desactivar(@PathVariable UUID id) {
        estudianteService.desactivarEstudiante(id);
        return ResponseEntity.noContent().build();
    }
}