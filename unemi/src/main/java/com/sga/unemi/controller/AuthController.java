package com.sga.unemi.controller;

import com.sga.unemi.dto.LoginRequest;
import com.sga.unemi.dto.LoginResponse;
import com.sga.unemi.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(
            @AuthenticationPrincipal String email) {
        authService.logout(email);
        return ResponseEntity.ok(Map.of("mensaje", "Sesión cerrada exitosamente"));
    }

    @GetMapping("/verificar")
    public ResponseEntity<Map<String, String>> verificar(
            @AuthenticationPrincipal String email) {
        return ResponseEntity.ok(Map.of("email", email, "estado", "Token válido"));
    }
}