package com.sga.unemi.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;
import java.util.UUID;

/**
 * Filtro que genera un identificador único de correlación ({@code traceId})
 * por cada petición HTTP, conforme a la sección "Manejo de Errores" del
 * documento de diseño ("Logs estructurados: Logback genera logs en formato
 * JSON con correlación de peticiones (traceId)").
 * <p>
 * El {@code traceId} se coloca en el {@link MDC} (Mapped Diagnostic
 * Context) de SLF4J, lo que hace que aparezca automáticamente en todas las
 * líneas de log generadas durante el procesamiento de esa petición,
 * facilitando rastrear todos los eventos relacionados con una sola
 * solicitud específica al revisar los logs (por ejemplo, en un incidente
 * de producción donde se necesita reconstruir qué pasó en una petición
 * particular).
 */
@Component
@Order(1)
public class TraceIdFilter extends GenericFilterBean {

    private static final String TRACE_ID_KEY = "traceId";

    /**
     * Genera un {@code traceId} nuevo, lo coloca en el MDC antes de
     * procesar la petición, y lo limpia al finalizar (incluso si la
     * petición termina en error), para evitar que un traceId se filtre
     * entre peticiones distintas atendidas por el mismo hilo.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        String traceId = UUID.randomUUID().toString();
        MDC.put(TRACE_ID_KEY, traceId);
        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove(TRACE_ID_KEY);
        }
    }
}