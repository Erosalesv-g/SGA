package com.sga.unemi.controller;

import com.sga.unemi.dto.AsistenciaResumenResponse;
import com.sga.unemi.dto.BoletinResponse;
import com.sga.unemi.dto.TrabajoBoletinMasivoResponse;
import com.sga.unemi.service.BoletinMasivoService;
import com.sga.unemi.service.ReportePdfService;
import com.sga.unemi.service.ReporteService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Controlador REST del módulo de reportes (RF-08: Generación de Reportes).
 * <p>
 * Expone tanto las versiones en formato JSON (para consumo por el frontend)
 * como en formato PDF (para descarga directa) de los boletines de
 * calificaciones y los resúmenes de asistencia, además de los endpoints
 * para iniciar y consultar trabajos de generación masiva de boletines
 * (RNF-0011), procesados de forma asíncrona vía RabbitMQ.
 */
@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    private final ReporteService reporteService;
    private final ReportePdfService reportePdfService;
    private final BoletinMasivoService boletinMasivoService;

    public ReporteController(ReporteService reporteService, ReportePdfService reportePdfService,
                              BoletinMasivoService boletinMasivoService) {
        this.reporteService = reporteService;
        this.reportePdfService = reportePdfService;
        this.boletinMasivoService = boletinMasivoService;
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

    /**
     * Inicia un trabajo de generación masiva de boletines en PDF para todos
     * los estudiantes de un nivel educativo (RNF-0011: Escalabilidad de
     * Usuarios).
     * <p>
     * La generación de los PDFs se procesa de forma asíncrona vía RabbitMQ;
     * este endpoint responde de inmediato con el trabajo en estado
     * PENDIENTE. El progreso se consulta con
     * {@link #obtenerTrabajoMasivo(UUID)}.
     *
     * @param nivel        nivel educativo (ej. "3°", "9no")
     * @param solicitadoPorId id del usuario que solicita el trabajo
     * @return el trabajo recién creado
     */
    @PostMapping("/boletines-masivos/{nivel}")
    public ResponseEntity<TrabajoBoletinMasivoResponse> iniciarBoletinesMasivos(
            @PathVariable String nivel, @RequestParam UUID solicitadoPorId) {
        return ResponseEntity.ok(boletinMasivoService.iniciar(nivel, solicitadoPorId));
    }

    /**
     * Consulta el estado de un trabajo de generación masiva de boletines.
     *
     * @param trabajoId id del trabajo
     * @return el estado actual (PENDIENTE, PROCESANDO, COMPLETADO,
     *         COMPLETADO_CON_ERRORES o FALLIDO) junto con el progreso
     */
    @GetMapping("/boletines-masivos/{trabajoId}")
    public ResponseEntity<TrabajoBoletinMasivoResponse> obtenerTrabajoMasivo(@PathVariable UUID trabajoId) {
        return ResponseEntity.ok(boletinMasivoService.obtenerTrabajo(trabajoId));
    }
}