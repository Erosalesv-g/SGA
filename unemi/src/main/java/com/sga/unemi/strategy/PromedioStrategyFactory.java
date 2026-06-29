package com.sga.unemi.strategy;

import org.springframework.stereotype.Component;

/**
 * Fábrica del patrón Strategy: decide en tiempo de ejecución qué
 * implementación de {@link PromedioStrategy} debe usarse según el nivel
 * educativo del estudiante.
 * <p>
 * Esto evita que el código cliente (por ejemplo, el servicio de
 * calificaciones) necesite conocer las reglas de negocio de cada nivel;
 * solo necesita pedirle a esta fábrica la estrategia correcta.
 */
@Component
public class PromedioStrategyFactory {

    private final PromedioBasicaStrategy basicaStrategy;
    private final PromedioBachilleratoStrategy bachilleratoStrategy;

    public PromedioStrategyFactory(PromedioBasicaStrategy basicaStrategy,
                                    PromedioBachilleratoStrategy bachilleratoStrategy) {
        this.basicaStrategy = basicaStrategy;
        this.bachilleratoStrategy = bachilleratoStrategy;
    }

    /**
     * Selecciona la estrategia de cálculo de promedio adecuada según el
     * nivel educativo del estudiante.
     * <p>
     * El nivel de Bachillerato se identifica porque su texto contiene el
     * símbolo de grado ({@code °}), por ejemplo {@code "1°"}, {@code "2°"},
     * {@code "3°"}. Cualquier otro valor (por ejemplo {@code "9no"}) se
     * considera Educación Básica.
     *
     * @param nivel nivel educativo del estudiante (ej. "3°", "9no")
     * @return la estrategia de promedio correspondiente al nivel
     */
    public PromedioStrategy obtenerStrategy(String nivel) {
        if (nivel != null && nivel.contains("°")) {
            return bachilleratoStrategy;
        }
        return basicaStrategy;
    }
}