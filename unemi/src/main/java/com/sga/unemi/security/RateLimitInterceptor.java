package com.sga.unemi.security;
 
import org.springframework.web.servlet.HandlerInterceptor;
 
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
 
public class RateLimitInterceptor implements HandlerInterceptor {
 
    // Maximo de peticiones permitidas por IP dentro de la ventana de tiempo
    private static final int MAX_REQUESTS_POR_MINUTO = 60;
    private static final long VENTANA_MS = 60_000;
 
    private static class Contador {
        AtomicInteger peticiones = new AtomicInteger(0);
        long inicioVentana = System.currentTimeMillis();
    }
 
    private final ConcurrentHashMap<String, Contador> contadoresPorIp = new ConcurrentHashMap<>();
 
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
 
    private String obtenerIpCliente(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}