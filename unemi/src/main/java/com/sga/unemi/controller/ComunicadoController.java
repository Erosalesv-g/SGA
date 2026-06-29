package com.sga.unemi.controller;

import com.sga.unemi.dto.ComunicadoRequest;
import com.sga.unemi.dto.ComunicadoResponse;
import com.sga.unemi.service.ComunicadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST del módulo de comunicados (RF-06: Comunicados).
 * <p>
 * El envío masivo de notificaciones a los destinatarios de un comunicado
 * no ocurre de forma síncrona en este controlador: se delega a RabbitMQ a
 * través de {@code ComunicadoService}, protegido con el patrón Circuit
 * Breaker (ver {@code ComunicadoEventPublisher}).
 */
@RestController
@RequestMapping("/api/comunicados")
public class ComunicadoController {

    private final ComunicadoService comunicadoService;

    public ComunicadoController(ComunicadoService comunicadoService) {
        this.comunicadoService = comunicadoService;
    }

    /**
     * Lista todos los comunicados, ordenados por fecha de envío descendente.
     *
     * @return la lista completa de comunicados
     */
    @GetMapping
    public ResponseEntity<List<ComunicadoResponse>> listar() {
        return ResponseEntity.ok(comunicadoService.listarTodos());
    }

    /**
     * Lista los comunicados dirigidos a un rol específico.
     *
     * @param rol nombre del rol destinatario (ej. "REPRESENTANTE")
     * @return los comunicados dirigidos a ese rol
     */
    @GetMapping("/rol/{rol}")
    public ResponseEntity<List<ComunicadoResponse>> listarPorRol(@PathVariable String rol) {
        return ResponseEntity.ok(comunicadoService.listarPorRol(rol));
    }

    /**
     * Crea un nuevo comunicado y encola el envío de notificaciones masivas
     * a sus destinatarios de forma asíncrona vía RabbitMQ.
     *
     * @param request datos del comunicado a crear
     * @return el comunicado creado
     */
    @PostMapping
    public ResponseEntity<ComunicadoResponse> crear(@RequestBody ComunicadoRequest request) {
        return ResponseEntity.ok(comunicadoService.crear(request));
    }

    /**
     * Obtiene un comunicado por su id.
     *
     * @param id id del comunicado
     * @return el comunicado solicitado
     */
    @GetMapping("/{id}")
    public ResponseEntity<ComunicadoResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(comunicadoService.obtener(id));
    }

    /**
     * Elimina un comunicado.
     *
     * @param id id del comunicado a eliminar
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
        comunicadoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}