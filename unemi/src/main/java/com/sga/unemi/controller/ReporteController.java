package com.sga.unemi.controller;

import com.sga.unemi.dto.AsistenciaResumenResponse;
import com.sga.unemi.dto.BoletinResponse;
import com.sga.unemi.service.ReportePdfService;
import com.sga.unemi.service.ReporteService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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
    private final ReportePdfService reportePdfService;

    public ReporteController(ReporteService reporteService, ReportePdfService reportePdfService) {
        this.reporteService = reporteService;
        this.reportePdfService = reportePdfService;
    }

    @GetMapping("/boletin/{estudianteId}")
    public ResponseEntity<BoletinResponse> boletin(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(reporteService.generarBoletin(estudianteId));
    }

    @GetMapping("/asistencia/{estudianteId}")
    public ResponseEntity<AsistenciaResumenResponse> resumenAsistencia(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(reporteService.generarResumenAsistencia(estudianteId));
    }

    @GetMapping("/boletin/{estudianteId}/pdf")
    public ResponseEntity<byte[]> boletinPdf(@PathVariable UUID estudianteId) {
        BoletinResponse boletin = reporteService.generarBoletin(estudianteId);
        byte[] pdf = reportePdfService.generarBoletinPdf(boletin);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("boletin.pdf").build().toString())
                .body(pdf);
    }

    @GetMapping("/asistencia/{estudianteId}/pdf")
    public ResponseEntity<byte[]> asistenciaPdf(@PathVariable UUID estudianteId) {
        AsistenciaResumenResponse resumen = reporteService.generarResumenAsistencia(estudianteId);
        byte[] pdf = reportePdfService.generarAsistenciaPdf(resumen);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename("resumen_asistencia.pdf").build().toString())
                .body(pdf);
    }
}