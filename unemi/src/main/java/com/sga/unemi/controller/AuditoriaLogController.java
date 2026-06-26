package com.sga.unemi.controller;

import com.sga.unemi.dto.AuditoriaLogResponse;
import com.sga.unemi.service.AuditoriaLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/auditoria")
public class AuditoriaLogController {

    private final AuditoriaLogService auditoriaLogService;

    public AuditoriaLogController(AuditoriaLogService auditoriaLogService) {
        this.auditoriaLogService = auditoriaLogService;
    }

    @GetMapping
    public ResponseEntity<List<AuditoriaLogResponse>> listar() {
        return ResponseEntity.ok(auditoriaLogService.listarTodos());
    }
}