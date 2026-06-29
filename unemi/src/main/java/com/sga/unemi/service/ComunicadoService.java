package com.sga.unemi.service;

import com.sga.unemi.dto.ComunicadoMensaje;
import com.sga.unemi.dto.ComunicadoRequest;
import com.sga.unemi.dto.ComunicadoResponse;
import com.sga.unemi.model.Comunicado;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.ComunicadoRepository;
import com.sga.unemi.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Lógica de negocio del módulo de comunicados (RF-06).
 * <p>
 * Tras guardar un comunicado, delega el envío masivo de notificaciones a
 * sus destinatarios mediante {@link ComunicadoEventPublisher}, que publica
 * el evento a RabbitMQ de forma asíncrona y protegida con el patrón Circuit
 * Breaker, para que esta operación no bloquee la petición HTTP ni falle por
 * completo si el broker de mensajería no está disponible.
 */
@Service
public class ComunicadoService {

    private final ComunicadoRepository comunicadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ComunicadoEventPublisher eventPublisher;

    public ComunicadoService(ComunicadoRepository comunicadoRepository,
                              UsuarioRepository usuarioRepository,
                              ComunicadoEventPublisher eventPublisher) {
        this.comunicadoRepository = comunicadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Lista todos los comunicados, ordenados por fecha de envío
     * descendente (los más recientes primero).
     *
     * @return la lista completa de comunicados
     */
    public List<ComunicadoResponse> listarTodos() {
        return comunicadoRepository.findAllByOrderByFechaEnvioDesc().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lista los comunicados dirigidos a un rol específico.
     *
     * @param rol nombre del rol destinatario
     * @return los comunicados dirigidos a ese rol
     */
    public List<ComunicadoResponse> listarPorRol(String rol) {
        return comunicadoRepository.findByDestinatarioRol(rol).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo comunicado y publica el evento correspondiente a
     * RabbitMQ para que el envío masivo de notificaciones se procese de
     * forma asíncrona.
     *
     * @param request datos del comunicado a crear
     * @return el comunicado creado
     * @throws RuntimeException si el remitente no existe
     */
    public ComunicadoResponse crear(ComunicadoRequest request) {
        Usuario remitente = usuarioRepository.findById(request.getRemitenteId())
                .orElseThrow(() -> new RuntimeException("Remitente no encontrado"));

        Comunicado comunicado = new Comunicado();
        comunicado.setTitulo(request.getTitulo());
        comunicado.setContenido(request.getContenido());
        comunicado.setRemitente(remitente);
        comunicado.setDestinatarioRol(request.getDestinatarioRol());

        Comunicado guardado = comunicadoRepository.save(comunicado);

        // Publica el evento a RabbitMQ a traves del publisher protegido con
        // Circuit Breaker: el envio masivo de notificaciones se procesa de forma
        // asincrona en el ComunicadoConsumer, sin bloquear esta peticion. Si
        // RabbitMQ no responde, el Circuit Breaker evita que esto tumbe la
        // creacion del comunicado (ver ComunicadoEventPublisher).
        eventPublisher.publicar(new ComunicadoMensaje(guardado.getId()));

        return toResponse(guardado);
    }

    /**
     * Obtiene un comunicado por su id.
     *
     * @param id id del comunicado
     * @return el comunicado solicitado
     * @throws RuntimeException si no existe un comunicado con ese id
     */
    public ComunicadoResponse obtener(UUID id) {
        Comunicado comunicado = comunicadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Comunicado no encontrado"));
        return toResponse(comunicado);
    }

    /**
     * Elimina un comunicado.
     *
     * @param id id del comunicado a eliminar
     */
    public void eliminar(UUID id) {
        comunicadoRepository.deleteById(id);
    }

    /**
     * Convierte una entidad {@link Comunicado} a su DTO de respuesta,
     * resolviendo el nombre del remitente.
     *
     * @param c la entidad de comunicado a convertir
     * @return el DTO de respuesta correspondiente
     */
    private ComunicadoResponse toResponse(Comunicado c) {
        return new ComunicadoResponse(
                c.getId(), c.getTitulo(), c.getContenido(),
                c.getRemitente().getId(), c.getRemitente().getNombre(),
                c.getDestinatarioRol(), c.getFechaEnvio()
        );
    }
}