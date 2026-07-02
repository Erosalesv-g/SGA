package com.sga.unemi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Configuración de seguridad del sistema (RNF-0003: Seguridad OWASP Top 10).
 * <p>
 * Implementa las siguientes protecciones contra las amenazas del OWASP Top 10:
 * <ul>
 *   <li><b>A01 - Broken Access Control:</b> Spring Security con RBAC de 6 roles;
 *       cada endpoint verifica el rol del token JWT antes de responder. Los datos
 *       también se filtran por rol en el backend (no solo el menú en el frontend).</li>
 *   <li><b>A02 - Cryptographic Failures:</b> Contraseñas hasheadas con BCrypt
 *       (factor de trabajo 12); nunca se almacenan ni transmiten en texto plano.
 *       Los tokens JWT usan HMAC-SHA384.</li>
 *   <li><b>A03 - Injection:</b> Todas las consultas a PostgreSQL se realizan vía
 *       JPA/Hibernate con parámetros enlazados (prepared statements); nunca
 *       se construyen queries concatenando entrada del usuario.</li>
 *   <li><b>A05 - Security Misconfiguration:</b> CSRF deshabilitado (API REST
 *       stateless con JWT, sin cookies de sesión); sesiones deshabilitadas
 *       ({@code SessionCreationPolicy.STATELESS}); CORS restringido al origen
 *       conocido del frontend; headers de seguridad habilitados por defecto
 *       por Spring Security ({@code X-Content-Type-Options: nosniff},
 *       {@code X-Frame-Options: DENY}, {@code X-XSS-Protection: 0}) más
 *       {@code Content-Security-Policy} configurado explícitamente.</li>
 *   <li><b>A07 - Authentication Failures:</b> JWT con expiración de 15 minutos;
 *       bloqueo de cuenta tras intentos fallidos (Redis); rate limiting de
 *       60 peticiones/min por IP ({@link RateLimitInterceptor}).</li>
 *   <li><b>A09 - Logging & Monitoring:</b> Módulo de auditoría (tabla
 *       {@code auditoria_log}) que registra todas las operaciones de creación,
 *       modificación y eliminación con actor, fecha y detalle de la operación.</li>
 * </ul>
 * Las protecciones contra XSS (A03) en el frontend están cubiertas por React,
 * que escapa automáticamente el contenido renderizado en el DOM.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    /**
     * Configura la cadena de filtros de seguridad HTTP.
     * <p>
     * Puntos clave de la configuración:
     * <ul>
     *   <li>CSRF deshabilitado: la API REST usa JWT (stateless), sin cookies
     *       de sesión que puedan ser objeto de ataques CSRF.</li>
     *   <li>CORS restringido: solo se permite el origen del frontend
     *       ({@code http://localhost:5173}), ver {@link #corsConfigurationSource()}.</li>
     *   <li>Headers de seguridad: Spring Security agrega automáticamente
     *       {@code X-Content-Type-Options}, {@code X-Frame-Options} y
     *       {@code X-XSS-Protection}; se agrega adicionalmente
     *       {@code Content-Security-Policy}.</li>
     *   <li>Sesiones deshabilitadas: {@code STATELESS} — cada petición debe
     *       incluir su propio token JWT.</li>
     *   <li>Auditoría restringida: solo el rol RECTOR puede acceder al log
     *       de auditoría.</li>
     * </ul>
     *
     * @param http el objeto de configuración de seguridad HTTP de Spring
     * @return la cadena de filtros configurada
     * @throws Exception si falla la construcción de la cadena de filtros
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .headers(headers -> headers
                .addHeaderWriter(new StaticHeadersWriter(
                    "Content-Security-Policy",
                    "default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; img-src 'self' data:; connect-src 'self' http://localhost:8080"
                ))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/auditoria/**").hasRole("RECTOR")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Configura la política CORS para restringir los orígenes permitidos
     * exclusivamente al frontend del sistema (RNF-0003: A05 Security
     * Misconfiguration).
     * <p>
     * Solo se permiten peticiones desde {@code http://localhost:5173} (Vite,
     * desarrollo local). Antes de desplegar a producción, agregar aquí el
     * dominio del hosting (por ejemplo, {@code https://sga-duran.duckdns.org}).
     *
     * @return la fuente de configuración CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "https://uefd.duckdns.org"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * Configura el codificador de contraseñas BCrypt con factor de trabajo 12
     * (RNF-0003: A02 Cryptographic Failures).
     * <p>
     * El factor 12 implica 2^12 = 4096 iteraciones de hashing, lo que hace
     * que los ataques de fuerza bruta sean computacionalmente costosos.
     *
     * @return el codificador BCrypt configurado
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Expone el {@code AuthenticationManager} de Spring Security como bean
     * para poder inyectarlo en el servicio de autenticación.
     *
     * @param config la configuración de autenticación de Spring Security
     * @return el gestor de autenticación
     * @throws Exception si no se puede obtener el gestor de autenticación
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}