package com.sga.unemi.controller;

import com.sga.unemi.dto.CalificacionRequest;
import com.sga.unemi.dto.CalificacionResponse;
import com.sga.unemi.service.CalificacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Controlador REST del módulo de calificaciones (RF-03: Control de
 * Calificaciones).
 * <p>
 * Expone las operaciones de creación, consulta, actualización y eliminación
 * de calificaciones, además del cálculo de promedios, el cual delega en el
 * patrón Strategy a través de {@link CalificacionService}.
 */
@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    /**
     * Lista todas las calificaciones registradas en el sistema.
     *
     * @return la lista completa de calificaciones
     */
    @GetMapping
    public ResponseEntity<List<CalificacionResponse>> listar() {
        return ResponseEntity.ok(calificacionService.listarTodas());
    }

    /**
     * Lista las calificaciones de un estudiante específico.
     *
     * @param estudianteId id del estudiante
     * @return las calificaciones de ese estudiante
     */
    @GetMapping("/estudiante/{estudianteId}")
    public ResponseEntity<List<CalificacionResponse>> listarPorEstudiante(@PathVariable UUID estudianteId) {
        return ResponseEntity.ok(calificacionService.listarPorEstudiante(estudianteId));
    }

    /**
     * Registra una nueva calificación.
     *
     * @param request datos de la calificación a registrar
     * @param actorId id del usuario que realiza la operación, para el
     *                registro de auditoría
     * @return la calificación creada
     */
    @PostMapping
    public ResponseEntity<CalificacionResponse> crear(@RequestBody CalificacionRequest request, @RequestParam UUID actorId) {
        return ResponseEntity.ok(calificacionService.crear(request, actorId));
    }

    /**
     * Obtiene una calificación por su id.
     *
     * @param id id de la calificación
     * @return la calificación solicitada
     */
    @GetMapping("/{id}")
    public ResponseEntity<CalificacionResponse> obtener(@PathVariable UUID id) {
        return ResponseEntity.ok(calificacionService.obtener(id));
    }

    /**
     * Actualiza una calificación existente.
     *
     * @param id      id de la calificación a actualizar
     * @param request nuevos datos de la calificación
     * @param actorId id del usuario que realiza la operación, para el
     *                registro de auditoría
     * @return la calificación actualizada
     */
    @PutMapping("/{id}")
    public ResponseEntity<CalificacionResponse> actualizar(@PathVariable UUID id, @RequestBody CalificacionRequest request, @RequestParam UUID actorId) {
        return ResponseEntity.ok(calificacionService.actualizar(id, request, actorId));
    }

    /**
     * Elimina una calificación.
     *
     * @param id      id de la calificación a eliminar
     * @param actorId id del usuario que realiza la operación, para el
     *                registro de auditoría
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable UUID id, @RequestParam UUID actorId) {
        calificacionService.eliminar(id, actorId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Calcula el promedio de un estudiante en una materia específica,
     * usando la estrategia de cálculo correspondiente a su nivel educativo
     * (ver patrón Strategy en {@code com.sga.unemi.strategy}).
     *
     * @param estudianteId id del estudiante
     * @param materiaId    id de la materia
     * @return el promedio calculado
     */
    @GetMapping("/promedio/{estudianteId}/{materiaId}")
    public ResponseEntity<Double> obtenerPromedio(@PathVariable UUID estudianteId, @PathVariable UUID materiaId) {
        return ResponseEntity.ok(calificacionService.calcularPromedio(estudianteId, materiaId));
    }
}