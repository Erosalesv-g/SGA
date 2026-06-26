package com.sga.unemi.controller;

import com.sga.unemi.dto.RecursoPedagogicoResponse;
import com.sga.unemi.model.RecursoPedagogico;
import com.sga.unemi.service.RecursoPedagogicoService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recursos")
public class RecursoPedagogicoController {

    private final RecursoPedagogicoService recursoService;

    public RecursoPedagogicoController(RecursoPedagogicoService recursoService) {
        this.recursoService = recursoService;
    }

    @GetMapping
    public ResponseEntity<List<RecursoPedagogicoResponse>> listar() {
        return ResponseEntity.ok(recursoService.listarTodos());
    }

    @GetMapping("/materia/{materiaId}")
    public ResponseEntity<List<RecursoPedagogicoResponse>> listarPorMateria(@PathVariable UUID materiaId) {
        return ResponseEntity.ok(recursoService.listarPorMateria(materiaId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RecursoPedagogicoResponse> subir(
            @RequestParam("titulo") String titulo,
            @RequestParam(value = "descripcion", required = false) String descripcion,
            @RequestParam("materiaId") UUID materiaId,
            @RequestParam("docenteId") UUID docenteId,
            @RequestParam("archivo") MultipartFile archivo) {
        return ResponseEntity.ok(recursoService.subir(titulo, descripcion, materiaId, docenteId, archivo));
    }

    @GetMapping("/{id}/descargar")
    public ResponseEntity<InputStreamResource> descargar(@PathVariable UUID id) {
        RecursoPedagogico recurso = recursoService.obtenerEntidad(id);
        InputStream stream = recursoService.descargar(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        recurso.getTipoArchivo() != null ? recurso.getTipoArchivo() : "application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(recurso.getNombreArchivo()).build().toString())
                .body(new InputStreamResource(stream));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        recursoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}