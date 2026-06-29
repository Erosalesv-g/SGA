package com.sga.unemi.strategy;

import com.sga.unemi.model.Calificacion;

import java.util.List;

/**
 * Patrón de diseño Strategy.
 * <p>
 * Define el contrato para las distintas formas de calcular el promedio de
 * un conjunto de calificaciones. Cada nivel educativo (Educación Básica,
 * Bachillerato) puede requerir una fórmula distinta, por lo que el algoritmo
 * de cálculo se encapsula en implementaciones intercambiables de esta
 * interfaz en lugar de usar condicionales dispersos por el código.
 *
 * @see PromedioBasicaStrategy
 * @see PromedioBachilleratoStrategy
 * @see PromedioStrategyFactory
 */
public interface PromedioStrategy {

    /**
     * Calcula el promedio final a partir de una lista de calificaciones.
     *
     * @param calificaciones lista de calificaciones a promediar; puede venir
     *                       vacía o nula, en cuyo caso las implementaciones
     *                       deben devolver 0.0
     * @return el promedio calculado según la fórmula de la estrategia concreta
     */
    Double calcularPromedio(List<Calificacion> calificaciones);
}