package com.sga.unemi.service;
 
import com.sga.unemi.config.RabbitMQConfig;
import com.sga.unemi.dto.ComunicadoMensaje;
import com.sga.unemi.dto.ComunicadoRequest;
import com.sga.unemi.dto.ComunicadoResponse;
import com.sga.unemi.model.Comunicado;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.ComunicadoRepository;
import com.sga.unemi.repository.UsuarioRepository;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
 
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
 
@Service
public class ComunicadoService {
 
    private final ComunicadoRepository comunicadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final RabbitTemplate rabbitTemplate;
 
    public ComunicadoService(ComunicadoRepository comunicadoRepository,
                              UsuarioRepository usuarioRepository,
                              RabbitTemplate rabbitTemplate) {
        this.comunicadoRepository = comunicadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.rabbitTemplate = rabbitTemplate;
    }
 
    public List<ComunicadoResponse> listarTodos() {
        return comunicadoRepository.findAllByOrderByFechaEnvioDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
 
    public List<ComunicadoResponse> listarPorRol(String rol) {
        return comunicadoRepository.findByDestinatarioRol(rol).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
 
    public ComunicadoResponse crear(ComunicadoRequest request) {
        Usuario remitente = usuarioRepository.findById(request.getRemitenteId())
                .orElseThrow(() -> new RuntimeException("Remitente no encontrado"));
 
        Comunicado comunicado = new Comunicado();
        comunicado.setTitulo(request.getTitulo());
        comunicado.setContenido(request.getContenido());
        comunicado.setRemitente(remitente);
        comunicado.setDestinatarioRol(request.getDestinatarioRol());
 
        Comunicado guardado = comunicadoRepository.save(comunicado);
 
        // Publica el evento a RabbitMQ: el envio masivo de notificaciones se
        // procesa de forma asincrona en el ComunicadoConsumer, sin bloquear esta peticion
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.COMUNICADOS_EXCHANGE,
                RabbitMQConfig.COMUNICADOS_ROUTING_KEY,
                new ComunicadoMensaje(guardado.getId())
        );
 
        return toResponse(guardado);
    }
 
    public ComunicadoResponse obtener(UUID id) {
        Comunicado comunicado = comunicadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comunicado no encontrado"));
        return toResponse(comunicado);
    }
 
    public void eliminar(UUID id) {
        comunicadoRepository.deleteById(id);
    }
 
    private ComunicadoResponse toResponse(Comunicado c) {
        return new ComunicadoResponse(
                c.getId(), c.getTitulo(), c.getContenido(),
                c.getRemitente().getId(), c.getRemitente().getNombre(),
                c.getDestinatarioRol(), c.getFechaEnvio()
        );
    }
}