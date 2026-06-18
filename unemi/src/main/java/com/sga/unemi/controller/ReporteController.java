package com.sga.unemi.controller;

import com.sga.unemi.dto.AsistenciaResumenResponse;
import com.sga.unemi.dto.BoletinResponse;
import com.sga.unemi.service.ReporteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;

    public ReporteController(ReporteService reporteService) {
        this.reporteService = reporteService;
    }

    @GetMapping("/boletin/{estudianteId}")
    public ResponseEntity<BoletinResponse> boletin(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(reporteService.generarBoletin(estudianteId));
    }

    @GetMapping("/asistencia/{estudianteId}")
    public ResponseEntity<AsistenciaResumenResponse> resumenAsistencia(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(reporteService.generarResumenAsistencia(estudianteId));
    }
}