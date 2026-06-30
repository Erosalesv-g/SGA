package com.sga.unemi.service;

import com.sga.unemi.dto.LoginRequest;
import com.sga.unemi.dto.LoginResponse;
import com.sga.unemi.exception.ValidationException;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.UsuarioRepository;
import com.sga.unemi.security.JwtUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * Lógica de autenticación del sistema (RF-01, RNF-0002).
 * <p>
 * Implementa bloqueo de cuenta tras intentos fallidos consecutivos (vía
 * Redis, con expiración automática) y notifica al usuario por correo
 * cuando su cuenta se bloquea, conforme a la sección "Seguridad" del
 * documento de diseño. El envío de correo es best-effort: ver
 * {@link EmailService} para el detalle de por qué un fallo de SMTP no
 * debe impedir que la cuenta se bloquee correctamente.
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RedisTemplate<String, String> redisTemplate;
    private final EmailService emailService;

    private static final int MAX_INTENTOS = 5;
    private static final long BLOQUEO_MINUTOS = 15;

    public AuthService(UsuarioRepository usuarioRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       RedisTemplate<String, String> redisTemplate,
                       EmailService emailService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.redisTemplate = redisTemplate;
        this.emailService = emailService;
    }

    /**
     * Autentica a un usuario y, si las credenciales son válidas, genera
     * sus tokens de acceso (RS256) y guarda la sesión en Redis.
     * <p>
     * Tras {@value #MAX_INTENTOS} intentos fallidos consecutivos, la
     * cuenta se bloquea por {@value #BLOQUEO_MINUTOS} minutos y se
     * notifica al usuario por correo electrónico.
     *
     * @param request credenciales de inicio de sesión
     * @return los tokens de acceso y refresh, junto con los datos básicos
     *         del usuario autenticado
     * @throws ValidationException si la cuenta está bloqueada, las
     *                              credenciales son inválidas, o el
     *                              usuario está inactivo
     */
    public LoginResponse login(LoginRequest request) {
        String email = request.getEmail();
        String claveBloqueo = "bloqueo:" + email;
        String claveIntentos = "intentos:" + email;

        // Verificar si está bloqueado
        String bloqueado = redisTemplate.opsForValue().get(claveBloqueo);
        if (bloqueado != null) {
            throw new ValidationException("Cuenta bloqueada. Intenta en " + BLOQUEO_MINUTOS + " minutos.");
        }

        // Buscar usuario
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationException("Credenciales inválidas"));

        if (!usuario.isActivo()) {
            throw new ValidationException("Usuario inactivo");
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

                emailService.notificarBloqueoCuenta(email, BLOQUEO_MINUTOS);

                throw new ValidationException("Cuenta bloqueada por " + BLOQUEO_MINUTOS + " minutos tras " + MAX_INTENTOS + " intentos fallidos.");
            }

            throw new ValidationException("Credenciales inválidas");
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

    /**
     * Cierra la sesión de un usuario, eliminando su token de la lista de
     * sesiones activas en Redis.
     *
     * @param email email del usuario que cierra sesión
     */
    public void logout(String email) {
        redisTemplate.delete("session:" + email);
    }
}