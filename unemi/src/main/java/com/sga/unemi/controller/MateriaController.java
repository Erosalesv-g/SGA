package com.sga.unemi.controller;

import com.sga.unemi.dto.MateriaRequest;
import com.sga.unemi.dto.MateriaResponse;
import com.sga.unemi.service.MateriaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    public ResponseEntity<List<MateriaResponse>> listar() {
        return ResponseEntity.ok(materiaService.listarMaterias());
    }

    @PostMapping
    public ResponseEntity<MateriaResponse> crear(@RequestBody MateriaRequest request) {
        return ResponseEntity.ok(materiaService.crearMateria(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MateriaResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(materiaService.obtenerMateria(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MateriaResponse> actualizar(@PathVariable UUID id, @RequestBody MateriaRequest request) {
        return ResponseEntity.ok(materiaService.actualizarMateria(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        materiaService.eliminarMateria(id);
        return ResponseEntity.noContent().build();
    }
}