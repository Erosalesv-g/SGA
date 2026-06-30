package com.sga.unemi.security;

import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Interceptor de limitación de tasa de peticiones (rate limiting) por
 * dirección IP, aplicado a todos los endpoints bajo {@code /api/**}.
 * <p>
 * Mantiene un contador en memoria por IP dentro de una ventana de tiempo
 * fija; si una IP supera el límite de peticiones permitidas, las peticiones
 * adicionales se rechazan con el código HTTP 429 (Too Many Requests) hasta
 * que la ventana se reinicie.
 */
public class RateLimitInterceptor implements HandlerInterceptor {

    // Maximo de peticiones permitidas por IP dentro de la ventana de tiempo
    private static final int MAX_REQUESTS_POR_MINUTO = 100;
    private static final long VENTANA_MS = 60_000;

    /**
     * Contador de peticiones de una IP dentro de la ventana de tiempo
     * actual, junto con la marca de tiempo en que empezó esa ventana.
     */
    private static class Contador {
        AtomicInteger peticiones = new AtomicInteger(0);
        long inicioVentana = System.currentTimeMillis();
    }

    private final ConcurrentHashMap<String, Contador> contadoresPorIp = new ConcurrentHashMap<>();

    /**
     * Verifica si la IP de origen de la petición ya alcanzó el límite de
     * peticiones permitidas en la ventana de tiempo actual.
     *
     * @param request  la petición HTTP entrante
     * @param response la respuesta HTTP, usada para devolver el error 429
     *                 si se excede el límite
     * @param handler  el manejador que atendería la petición
     * @return {@code true} si la petición puede continuar; {@code false}
     *         si se rechazó por exceder el límite de tasa
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String ip = obtenerIpCliente(request);

        Contador contador = contadoresPorIp.computeIfAbsent(ip, k -> new Contador());

        synchronized (contador) {
            long ahora = System.currentTimeMillis();

            // Si ya paso la ventana de tiempo, reiniciar el contador
            if (ahora - contador.inicioVentana > VENTANA_MS) {
                contador.peticiones.set(0);
                contador.inicioVentana = ahora;
            }

            int peticionesActuales = contador.peticiones.incrementAndGet();

            if (peticionesActuales > MAX_REQUESTS_POR_MINUTO) {
                response.setStatus(429); // Too Many Requests
                response.setContentType("application/json");
                response.getWriter().write(
                        "{\"error\": \"Demasiadas peticiones. Intenta de nuevo en unos segundos.\"}"
                );
                return false;
            }
        }

        return true;
    }

    /**
     * Obtiene la dirección IP real del cliente, considerando el encabezado
     * {@code X-Forwarded-For} (presente cuando la petición pasa por un
     * proxy o balanceador de carga) antes de recurrir a la IP directa de
     * la conexión TCP.
     *
     * @param request la petición HTTP entrante
     * @return la dirección IP del cliente
     */
    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}