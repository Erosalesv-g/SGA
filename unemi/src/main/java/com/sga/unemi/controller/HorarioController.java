package com.sga.unemi.controller;

import com.sga.unemi.dto.HorarioRequest;
import com.sga.unemi.dto.HorarioResponse;
import com.sga.unemi.service.HorarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/horarios")
public class HorarioController {

    private final HorarioService horarioService;

    public HorarioController(HorarioService horarioService) {
        this.horarioService = horarioService;
    }

    @GetMapping
    public ResponseEntity<List<HorarioResponse>> listar() {
        return ResponseEntity.ok(horarioService.listarTodos());
    }

    @GetMapping("/docente/{docenteId}")
    public ResponseEntity<List<HorarioResponse>> listarPorDocente(@PathVariable UUID docenteId) {
        return ResponseEntity.ok(horarioService.listarPorDocente(docenteId));
    }

    @PostMapping
    public ResponseEntity<HorarioResponse> crear(@RequestBody HorarioRequest request) {
        return ResponseEntity.ok(horarioService.crear(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HorarioResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(horarioService.obtener(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HorarioResponse> actualizar(@PathVariable UUID id, @RequestBody HorarioRequest request) {
        return ResponseEntity.ok(horarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        horarioService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}