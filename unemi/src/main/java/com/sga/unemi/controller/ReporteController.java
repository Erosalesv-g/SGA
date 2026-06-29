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

/**
 * Controlador REST del módulo de reportes (RF-08: Generación de Reportes).
 * <p>
 * Expone tanto las versiones en formato JSON (para consumo por el frontend)
 * como en formato PDF (para descarga directa) de los boletines de
 * calificaciones y los resúmenes de asistencia.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final ReportePdfService reportePdfService;

    public ReporteController(ReporteService reporteService, ReportePdfService reportePdfService) {
        this.reporteService = reporteService;
        this.reportePdfService = reportePdfService;
    }

    /**
     * Obtiene el boletín de calificaciones de un estudiante en formato JSON.
     *
     * @param estudianteId id del estudiante
     * @return el boletín con el promedio por materia y el promedio general
     */
    @GetMapping("/boletin/{estudianteId}")
    public ResponseEntity<BoletinResponse> boletin(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(reporteService.generarBoletin(estudianteId));
    }

    /**
     * Obtiene el resumen de asistencia de un estudiante en formato JSON.
     *
     * @param estudianteId id del estudiante
     * @return el resumen con los totales de presente, ausente, justificado
     *         y tardanza, además del porcentaje de asistencia
     */
    @GetMapping("/asistencia/{estudianteId}")
    public ResponseEntity<AsistenciaResumenResponse> resumenAsistencia(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(reporteService.generarResumenAsistencia(estudianteId));
    }

    /**
     * Genera y descarga el boletín de calificaciones de un estudiante como
     * archivo PDF, usando OpenPDF.
     *
     * @param estudianteId id del estudiante
     * @return el archivo PDF como adjunto descargable
     */
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

    /**
     * Genera y descarga el resumen de asistencia de un estudiante como
     * archivo PDF, usando OpenPDF.
     *
     * @param estudianteId id del estudiante
     * @return el archivo PDF como adjunto descargable
     */
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