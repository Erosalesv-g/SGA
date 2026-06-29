package com.sga.unemi.observer;

import com.sga.unemi.model.Calificacion;

/**
 * Patrón de diseño Observer.
 * <p>
 * Define el contrato que deben implementar los componentes interesados en
 * reaccionar cada vez que se registra o modifica una calificación, sin que
 * {@code CalificacionService} (el "sujeto" observado) necesite conocer los
 * detalles de qué hace cada observador.
 * <p>
 * Todos los componentes Spring que implementen esta interfaz se inyectan
 * automáticamente como una lista en {@code CalificacionService}, el cual los
 * notifica después de cada operación de creación o actualización.
 *
 * @see RepresentanteNotificacionObserver
 */
public interface CalificacionObserver {

    /**
     * Se invoca cada vez que se registra o actualiza una calificación.
     *
     * @param calificacion la calificación que acaba de registrarse o
     *                     actualizarse
     */
    void onCalificacionRegistrada(Calificacion calificacion);
}