package com.sga.unemi.observer;

import com.sga.unemi.model.Calificacion;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.model.Notificacion;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.NotificacionRepository;
import org.springframework.stereotype.Component;

@Component
public class RepresentanteNotificacionObserver implements CalificacionObserver {

    private final NotificacionRepository notificacionRepository;

    public RepresentanteNotificacionObserver(NotificacionRepository notificacionRepository) {
        this.notificacionRepository = notificacionRepository;
    }

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