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