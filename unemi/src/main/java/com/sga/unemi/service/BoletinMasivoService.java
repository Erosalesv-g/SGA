package com.sga.unemi.service;

import com.sga.unemi.dto.BoletinMasivoMensaje;
import com.sga.unemi.dto.TrabajoBoletinMasivoResponse;
import com.sga.unemi.model.EstadoTrabajo;
import com.sga.unemi.model.TrabajoBoletinMasivo;
import com.sga.unemi.model.Usuario;
import com.sga.unemi.repository.EstudianteRepository;
import com.sga.unemi.repository.TrabajoBoletinMasivoRepository;
import com.sga.unemi.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Orquesta la generación masiva de boletines (RF-08, RNF-0011).
 * <p>
 * Crea el registro de seguimiento del trabajo, cuenta cuántos estudiantes
 * tiene el nivel solicitado, y delega el procesamiento pesado (generar y
 * subir cada PDF) al consumidor asíncrono vía
 * {@link BoletinMasivoEventPublisher}, protegido con Circuit Breaker.
 */
@Service
public class BoletinMasivoService {

    private final TrabajoBoletinMasivoRepository trabajoRepository;
    private final EstudianteRepository estudianteRepository;
    private final UsuarioRepository usuarioRepository;
    private final BoletinMasivoEventPublisher eventPublisher;

    public BoletinMasivoService(TrabajoBoletinMasivoRepository trabajoRepository,
                                 EstudianteRepository estudianteRepository,
                                 UsuarioRepository usuarioRepository,
                                 BoletinMasivoEventPublisher eventPublisher) {
        this.trabajoRepository = trabajoRepository;
        this.estudianteRepository = estudianteRepository;
        this.usuarioRepository = usuarioRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Inicia un trabajo de generación masiva de boletines para todos los
     * estudiantes de un nivel educativo.
     * <p>
     * Esta operación responde de inmediato con el trabajo en estado
     * PENDIENTE; la generación real de los PDFs ocurre de forma asíncrona
     * en {@code BoletinMasivoConsumer}. El progreso se consulta mediante
     * {@link #obtenerTrabajo(UUID)}.
     *
     * @param nivel        nivel educativo para el que se generan los boletines
     * @param solicitadoPorId id del usuario (RECTOR) que solicita el trabajo
     * @return el trabajo recién creado, en estado PENDIENTE
     * @throws NoSuchElementException si el usuario solicitante no existe
     */
    public TrabajoBoletinMasivoResponse iniciar(String nivel, UUID solicitadoPorId) {
        Usuario solicitante = usuarioRepository.findById(solicitadoPorId)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));

        TrabajoBoletinMasivo trabajo = new TrabajoBoletinMasivo();
        trabajo.setNivel(nivel);
        trabajo.setEstado(EstadoTrabajo.PENDIENTE);
        trabajo.setTotalEstudiantes(estudianteRepository.findByNivel(nivel).size());
        trabajo.setSolicitadoPor(solicitante);

        TrabajoBoletinMasivo guardado = trabajoRepository.save(trabajo);

        eventPublisher.publicar(new BoletinMasivoMensaje(guardado.getId()));

        return toResponse(guardado);
    }

    /**
     * Consulta el estado actual de un trabajo de generación masiva.
     *
     * @param trabajoId id del trabajo
     * @return el estado actual del trabajo (PENDIENTE, PROCESANDO,
     *         COMPLETADO, COMPLETADO_CON_ERRORES o FALLIDO)
     * @throws NoSuchElementException si el trabajo no existe
     */
    public TrabajoBoletinMasivoResponse obtenerTrabajo(UUID trabajoId) {
        TrabajoBoletinMasivo trabajo = trabajoRepository.findById(trabajoId)
                .orElseThrow(() -> new NoSuchElementException("Trabajo no encontrado"));
        return toResponse(trabajo);
    }

    private TrabajoBoletinMasivoResponse toResponse(TrabajoBoletinMasivo t) {
        return new TrabajoBoletinMasivoResponse(
                t.getId(), t.getNivel(), t.getEstado().name(),
                t.getTotalEstudiantes(), t.getProcesados(), t.getFallidos(),
                t.getFechaInicio(), t.getFechaFin()
        );
    }
}