package com.sga.unemi.observer;

import com.sga.unemi.model.Calificacion;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.model.Notificacion;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.NotificacionRepository;
import org.springframework.stereotype.Component;

/**
 * Observador concreto del patrón Observer.
 * <p>
 * Cada vez que se registra una nueva calificación, genera automáticamente
 * una notificación dirigida al representante legal del estudiante, para que
 * pueda ver de inmediato que se publicó una nota nueva sin tener que entrar
 * a revisar manualmente el sistema.
 */
@Component
public class RepresentanteNotificacionObserver implements CalificacionObserver {

    private final NotificacionRepository notificacionRepository;

    public RepresentanteNotificacionObserver(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Crea una notificación para el representante legal del estudiante,
     * describiendo la materia y el valor de la calificación registrada.
     * Si el estudiante no tiene representante asociado, no hace nada.
     *
     * @param calificacion la calificación recién registrada o actualizada
     */
    @Override
    public void onCalificacionRegistrada(Calificacion calificacion) {
        Estudiante estudiante = calificacion.getEstudiante();
        Usuario representante = estudiante.getRepresentante();

        // Si el estudiante no tiene representante asociado, no hay a quién notificar
        if (representante == null) {
            return;
        }

        Notificacion notificacion = new Notificacion();
        notificacion.setDestinatario(representante);
        notificacion.setTipo("CALIFICACION");
        notificacion.setMensaje("Se registró una nueva calificación de " +
                estudiante.getNombre() + " en " + calificacion.getMateria().getNombre() +
                ": " + calificacion.getValor() + " (" + calificacion.getTipo() + ")");
        notificacion.setReferenciaId(calificacion.getId());

        notificacionRepository.save(notificacion);
    }
}