package com.sga.unemi.security;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.UUID;

/**
 * Utilidad de generación y validación de tokens JWT (RNF-0002), firmados
 * con el algoritmo asimétrico RS256 conforme a la sección "Seguridad" del
 * documento de diseño.
 * <p>
 * A diferencia de un algoritmo simétrico (como HS256/HS384, donde la misma
 * clave firma y verifica), RS256 usa un par de claves: la clave privada
 * firma los tokens y nunca sale del backend; la clave pública podría
 * compartirse con otros servicios para que verifiquen tokens sin poder
 * falsificarlos, lo cual es relevante si el sistema evoluciona hacia una
 * arquitectura con múltiples servicios (ver sección "Arquitectura y
 * Patrones de Diseño" del documento).
 * <p>
 * El par de claves se genera en memoria al iniciar la aplicación. Esto
 * implica que los tokens emitidos antes de un reinicio del backend dejan
 * de ser válidos después de reiniciar (la clave pública para verificarlos
 * cambia). Para un despliegue con múltiples instancias del backend, las
 * claves deberían generarse una vez y compartirse de forma segura entre
 * instancias (por ejemplo, vía variables de entorno o un gestor de
 * secretos), en lugar de regenerarse en cada arranque.
 */
@Component
public class JwtUtil {

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.refresh-expiration}")
    private long refreshExpiration;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    /**
     * Genera el par de claves RSA (2048 bits) al iniciar la aplicación.
     *
     * @throws IllegalStateException si el algoritmo RSA no está disponible
     *                                en el entorno de ejecución (no debería
     *                                ocurrir en una JVM estándar)
     */
    @PostConstruct
    public void inicializarClaves() {
        try {
            KeyPairGenerator generador = KeyPairGenerator.getInstance("RSA");
            generador.initialize(2048);
            KeyPair par = generador.generateKeyPair();
            this.privateKey = (RSAPrivateKey) par.getPrivate();
            this.publicKey = (RSAPublicKey) par.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No se pudo generar el par de claves RSA para JWT", e);
        }
    }

    /**
     * Genera un access token firmado con RS256 para un usuario autenticado.
     *
     * @param email  email del usuario (se usa como subject del token)
     * @param rol    rol del usuario, incluido como claim personalizado
     * @param userId id del usuario, incluido como claim personalizado
     * @return el token JWT firmado, listo para enviar en la cabecera Authorization
     */
    public String generateToken(String email, String rol, UUID userId) {
        return Jwts.builder()
                .subject(email)
                .claim("rol", rol)
                .claim("userId", userId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Genera un refresh token firmado con RS256, de mayor duración que el
     * access token, usado para renovar la sesión sin requerir login nuevamente.
     *
     * @param email email del usuario
     * @return el refresh token firmado
     */
    public String generateRefreshToken(String email) {
        return Jwts.builder()
                .subject(email)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + refreshExpiration))
                .signWith(privateKey, Jwts.SIG.RS256)
                .compact();
    }

    /**
     * Extrae el email (subject) de un token válido.
     *
     * @param token el token JWT
     * @return el email contenido en el token
     */
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Extrae el rol de un token válido.
     *
     * @param token el token JWT
     * @return el rol contenido en el token
     */
    public String extractRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    /**
     * Verifica si un token es válido: firmado correctamente con la clave
     * pública del sistema y no expirado.
     *
     * @param token el token JWT a validar
     * @return {@code true} si el token es válido; {@code false} en caso
     *         contrario (firma inválida, expirado, formato incorrecto, etc.)
     */
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /**
     * Extrae y verifica los claims de un token usando la clave pública
     * RSA del sistema.
     *
     * @param token el token JWT
     * @return los claims del token
     * @throws JwtException si la firma no es válida, el token está
     *                       expirado, o el formato es incorrecto
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(publicKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}