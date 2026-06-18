package com.sga.unemi.service;

import com.sga.unemi.dto.LoginRequest;
import com.sga.unemi.dto.LoginResponse;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.UsuarioRepository;
import com.sga.unemi.security.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MINUTOS = 15;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RedisTemplate<String, String> redisTemplate) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
    }

    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();
        String claveBloqueo = "bloqueo:" + email;
        String claveIntentos = "intentos:" + email;

        // Verificar si está bloqueado
        String bloqueado = redisTemplate.opsForValue().get(claveBloqueo);
        if (bloqueado != null) {
            throw new RuntimeException("Cuenta bloqueada. Intenta en " + BLOQUEO_MINUTOS + " minutos.");
        }

        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new RuntimeException("Usuario inactivo");
        }

        // Verificar contraseña
        if (!passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())) {
            // Incrementar intentos fallidos
            Long intentos = redisTemplate.opsForValue().increment(claveIntentos);
            redisTemplate.expire(claveIntentos, Duration.ofMinutes(BLOQUEO_MINUTOS));

            if (intentos != null && intentos >= MAX_INTENTOS) {
                redisTemplate.opsForValue().set(claveBloqueo, "bloqueado",
                        Duration.ofMinutes(BLOQUEO_MINUTOS));
                redisTemplate.delete(claveIntentos);
                throw new RuntimeException("Cuenta bloqueada por " + BLOQUEO_MINUTOS + " minutos tras " + MAX_INTENTOS + " intentos fallidos.");
            }

            throw new RuntimeException("Credenciales inválidas");
        }

        // Login exitoso - limpiar intentos
        redisTemplate.delete(claveIntentos);

        // Generar tokens
        String accessToken = jwtUtil.generateToken(
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getId()
        );
        String refreshToken = jwtUtil.generateRefreshToken(usuario.getEmail());

        // Guardar sesión en Redis
        redisTemplate.opsForValue().set(
                "session:" + usuario.getEmail(),
                accessToken,
                Duration.ofMinutes(15)
        );

        return new LoginResponse(
                accessToken,
                refreshToken,
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getNombre()
        );
    }

    public void logout(String email) {
        redisTemplate.delete("session:" + email);
    }
}