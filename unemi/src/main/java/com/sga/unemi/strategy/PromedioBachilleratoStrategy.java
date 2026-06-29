package com.sga.unemi.strategy;

import com.sga.unemi.model.Calificacion;
import com.sga.unemi.model.TipoCalif;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementación concreta del patrón Strategy para Bachillerato.
 * <p>
 * A diferencia de {@link PromedioBasicaStrategy}, esta estrategia calcula
 * un promedio ponderado según el tipo de evaluación, conforme al reglamento
 * del Ministerio de Educación del Ecuador para el nivel de Bachillerato:
 * <ul>
 *   <li>Examen: 40%</li>
 *   <li>Parcial: 30%</li>
 *   <li>Proyecto: 20%</li>
 *   <li>Tarea: 10%</li>
 * </ul>
 */
@Component
public class PromedioBachilleratoStrategy implements PromedioStrategy {

    private static final double PESO_EXAMEN = 0.40;
    private static final double PESO_PARCIAL = 0.30;
    private static final double PESO_PROYECTO = 0.20;
    private static final double PESO_TAREA = 0.10;

    /**
     * {@inheritDoc}
     *
     * @return el promedio ponderado (40/30/20/10) por tipo de evaluación,
     *         o 0.0 si la lista está vacía o es nula
     */
    @Override
    public Double calcularPromedio(List<Calificacion> calificaciones) {
        if (calificaciones == null || calificaciones.isEmpty()) {
            return 0.0;
        }

        double promedioExamen = promedioPorTipo(calificaciones, TipoCalif.EXAMEN);
        double promedioParcial = promedioPorTipo(calificaciones, TipoCalif.PARCIAL);
        double promedioProyecto = promedioPorTipo(calificaciones, TipoCalif.PROYECTO);
        double promedioTarea = promedioPorTipo(calificaciones, TipoCalif.TAREA);

        return (promedioExamen * PESO_EXAMEN)
                + (promedioParcial * PESO_PARCIAL)
                + (promedioProyecto * PESO_PROYECTO)
                + (promedioTarea * PESO_TAREA);
    }

    /**
     * Calcula el promedio simple de las calificaciones que coinciden con un
     * tipo de evaluación específico.
     *
     * @param calificaciones lista completa de calificaciones del estudiante
     * @param tipo           tipo de evaluación a filtrar (EXAMEN, PARCIAL, etc.)
     * @return el promedio de las calificaciones de ese tipo, o 0.0 si no hay
     *         ninguna calificación de ese tipo
     */
    private double promedioPorTipo(List<Calificacion> calificaciones, TipoCalif tipo) {
        List<Double> valores = calificaciones.stream()
                .filter(c -> c.getTipo() == tipo)
                .map(Calificacion::getValor)
                .toList();

        if (valores.isEmpty()) {
            return 0.0;
        }

        return valores.stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
    }
}