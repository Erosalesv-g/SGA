package com.sga.unemi.strategy;

import com.sga.unemi.model.Calificacion;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementación concreta del patrón Strategy para Educación Básica.
 * <p>
 * Calcula el promedio como la media aritmética simple de todas las
 * calificaciones registradas, sin distinguir por tipo de evaluación
 * (examen, parcial, proyecto, tarea).
 */
@Component
public class PromedioBasicaStrategy implements PromedioStrategy {

    /**
     * {@inheritDoc}
     *
     * @return el promedio aritmético simple de las calificaciones, o 0.0 si
     *         la lista está vacía o es nula
     */
    @Override
    public Double calcularPromedio(List<Calificacion> calificaciones) {
        if (calificaciones == null || calificaciones.isEmpty()) {
            return 0.0;
        }

        return calificaciones.stream()
                .map(Calificacion::getValor)
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}