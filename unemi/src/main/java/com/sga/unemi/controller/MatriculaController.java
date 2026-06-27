package com.sga.unemi.controller;

import com.sga.unemi.dto.MatriculaRequest;
import com.sga.unemi.dto.MatriculaResponse;
import com.sga.unemi.service.MatriculaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @GetMapping
    public ResponseEntity<List<MatriculaResponse>> listar() {
        return ResponseEntity.ok(matriculaService.listarMatriculas());
    }

    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<MatriculaResponse>> listarPorEstudiante(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(matriculaService.listarPorEstudiante(estudianteId));
    }

    @PostMapping
    public ResponseEntity<MatriculaResponse> registrar(@RequestBody MatriculaRequest request) {
        return ResponseEntity.ok(matriculaService.registrar(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MatriculaResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(matriculaService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MatriculaResponse> actualizar(@PathVariable UUID id, @RequestBody MatriculaRequest request) {
        return ResponseEntity.ok(matriculaService.actualizar(id, request));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<Void> anular(@PathVariable UUID id) {
        matriculaService.anular(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/reactivar")
    public ResponseEntity<Void> reactivar(@PathVariable UUID id) {
        matriculaService.reactivar(id);
        return ResponseEntity.noContent().build();
    }
}