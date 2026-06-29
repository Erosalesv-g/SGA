package com.sga.unemi.consumer;

import com.sga.unemi.config.RabbitMQConfig;
import com.sga.unemi.dto.ComunicadoMensaje;
import com.sga.unemi.model.Comunicado;
import com.sga.unemi.model.Notificacion;
import com.sga.unemi.model.Rol;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.ComunicadoRepository;
import com.sga.unemi.repository.NotificacionRepository;
import com.sga.unemi.repository.UsuarioRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Consumidor de la cola de comunicados (RF-06, RNF-0011).
 * <p>
 * Procesa en segundo plano los eventos publicados por
 * {@link com.sga.unemi.service.ComunicadoEventPublisher}: por cada
 * comunicado nuevo, busca a todos los usuarios del rol destinatario y les
 * crea una notificación individual. Este trabajo pesado se ejecuta de forma
 * asíncrona para que la creación del comunicado no tenga que esperar a que
 * se notifique a, potencialmente, cientos de representantes legales.
 */
@Component
public class ComunicadoConsumer {

    private final ComunicadoRepository comunicadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final NotificacionRepository notificacionRepository;

    public ComunicadoConsumer(ComunicadoRepository comunicadoRepository,
                               UsuarioRepository usuarioRepository,
                               NotificacionRepository notificacionRepository) {
        this.comunicadoRepository = comunicadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.notificacionRepository = notificacionRepository;
    }

    /**
     * Escucha la cola de comunicados y, por cada mensaje recibido, genera
     * una notificación para cada usuario del rol destinatario del
     * comunicado correspondiente.
     * <p>
     * Si el comunicado referenciado ya no existe en la base de datos (por
     * ejemplo, si fue eliminado antes de que se procesara el mensaje), la
     * operación se ignora silenciosamente.
     *
     * @param mensaje evento recibido de la cola, con el id del comunicado
     */
    @RabbitListener(queues = RabbitMQConfig.COMUNICADOS_QUEUE)
    public void procesarComunicado(ComunicadoMensaje mensaje) {
        Comunicado comunicado = comunicadoRepository.findById(mensaje.getComunicadoId())
                .orElse(null);

        if (comunicado == null) {
            return;
        }

        List<Usuario> destinatarios = obtenerDestinatarios(comunicado.getDestinatarioRol());

        for (Usuario destinatario : destinatarios) {
            Notificacion notificacion = new Notificacion();
            notificacion.setDestinatario(destinatario);
            notificacion.setTipo("COMUNICADO");
            notificacion.setMensaje(comunicado.getTitulo() + ": " + comunicado.getContenido());
            notificacion.setReferenciaId(comunicado.getId());
            notificacionRepository.save(notificacion);
        }
    }

    /**
     * Resuelve la lista de usuarios destinatarios de un comunicado según el
     * texto de rol almacenado.
     *
     * @param destinatarioRol texto del rol destinatario, o {@code "TODOS"}
     *                        (o {@code null}) para enviar a todos los usuarios
     * @return la lista de usuarios destinatarios; lista vacía si el texto
     *         no corresponde a ningún {@link Rol} válido
     */
    private List<Usuario> obtenerDestinatarios(String destinatarioRol) {
        if (destinatarioRol == null || destinatarioRol.equalsIgnoreCase("TODOS")) {
            return usuarioRepository.findAll();
        }

        try {
            Rol rol = Rol.valueOf(destinatarioRol.toUpperCase());
            return usuarioRepository.findByRol(rol);
        } catch (IllegalArgumentException e) {
            // El valor de destinatarioRol no coincide con ningun Rol valido
            return List.of();
        }
    }
}