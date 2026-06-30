package com.sga.unemi.consumer;

import com.sga.unemi.config.RabbitMQConfig;
import com.sga.unemi.dto.BoletinMasivoMensaje;
import com.sga.unemi.dto.BoletinResponse;
import com.sga.unemi.model.Estudiante;
import com.sga.unemi.model.EstadoTrabajo;
import com.sga.unemi.model.TrabajoBoletinMasivo;
import com.sga.unemi.repository.EstudianteRepository;
import com.sga.unemi.repository.TrabajoBoletinMasivoRepository;
import com.sga.unemi.service.ReportePdfService;
import com.sga.unemi.service.ReporteService;
import io.minio.PutObjectArgs;
import io.minio.MinioClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Consumidor de la cola de boletines masivos (RF-08, RNF-0011).
 * <p>
 * Procesa en segundo plano los trabajos publicados por
 * {@link com.sga.unemi.service.BoletinMasivoEventPublisher}: por cada
 * estudiante del nivel solicitado, genera su boletín en PDF
 * ({@link ReportePdfService}) y lo sube a MinIO, sin que la petición HTTP
 * original tenga que esperar a que termine todo el lote. El progreso se
 * va actualizando en el registro {@link TrabajoBoletinMasivo}
 * correspondiente para que el frontend pueda consultarlo.
 */
@Component
public class BoletinMasivoConsumer {

    private static final Logger log = LoggerFactory.getLogger(BoletinMasivoConsumer.class);

    private final TrabajoBoletinMasivoRepository trabajoRepository;
    private final EstudianteRepository estudianteRepository;
    private final ReporteService reporteService;
    private final ReportePdfService reportePdfService;
    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    public BoletinMasivoConsumer(TrabajoBoletinMasivoRepository trabajoRepository,
                                  EstudianteRepository estudianteRepository,
                                  ReporteService reporteService,
                                  ReportePdfService reportePdfService,
                                  MinioClient minioClient) {
        this.trabajoRepository = trabajoRepository;
        this.estudianteRepository = estudianteRepository;
        this.reporteService = reporteService;
        this.reportePdfService = reportePdfService;
        this.minioClient = minioClient;
    }

    /**
     * Escucha la cola de boletines masivos y procesa el trabajo
     * correspondiente: genera el boletín en PDF de cada estudiante del
     * nivel indicado y lo sube a MinIO bajo la ruta
     * {@code boletines-masivos/{trabajoId}/{estudianteId}.pdf}.
     * <p>
     * Si un boletín individual falla (por ejemplo, error al subir a
     * MinIO), se cuenta como fallido y se continúa con el resto, en vez de
     * abortar todo el lote por un solo estudiante problemático.
     *
     * @param mensaje evento recibido de la cola, con el id del trabajo
     */
    @RabbitListener(queues = RabbitMQConfig.BOLETINES_MASIVOS_QUEUE)
    public void procesarTrabajo(BoletinMasivoMensaje mensaje) {
        TrabajoBoletinMasivo trabajo = trabajoRepository.findById(mensaje.getTrabajoId())
                .orElse(null);

        if (trabajo == null) {
            log.warn("Trabajo de boletines masivos {} no encontrado, se ignora el mensaje", mensaje.getTrabajoId());
            return;
        }

        trabajo.setEstado(EstadoTrabajo.PROCESANDO);
        trabajoRepository.save(trabajo);

        List<Estudiante> estudiantes = estudianteRepository.findByNivel(trabajo.getNivel());
        trabajo.setTotalEstudiantes(estudiantes.size());

        int procesados = 0;
        int fallidos = 0;

        for (Estudiante estudiante : estudiantes) {
            try {
                generarYSubirBoletin(trabajo.getId(), estudiante.getId());
                procesados++;
            } catch (Exception e) {
                fallidos++;
                log.error("Fallo al generar el boletin del estudiante {} en el trabajo {}: {}",
                        estudiante.getId(), trabajo.getId(), e.getMessage());
            }
        }

        trabajo.setProcesados(procesados);
        trabajo.setFallidos(fallidos);
        trabajo.setFechaFin(LocalDateTime.now());
        trabajo.setEstado(fallidos == 0 ? EstadoTrabajo.COMPLETADO : EstadoTrabajo.COMPLETADO_CON_ERRORES);
        trabajoRepository.save(trabajo);

        log.info("Trabajo de boletines masivos {} finalizado: {} procesados, {} fallidos de {} estudiantes",
                trabajo.getId(), procesados, fallidos, estudiantes.size());
    }

    /**
     * Genera el boletín en PDF de un estudiante y lo sube a MinIO.
     *
     * @param trabajoId    id del trabajo de generación masiva (usado para
     *                     organizar la ruta de almacenamiento en MinIO)
     * @param estudianteId id del estudiante cuyo boletín se genera
     * @throws Exception si falla la generación del PDF o la subida a MinIO
     */
    private void generarYSubirBoletin(java.util.UUID trabajoId, java.util.UUID estudianteId) throws Exception {
        BoletinResponse boletin = reporteService.generarBoletin(estudianteId);
        byte[] pdf = reportePdfService.generarBoletinPdf(boletin);

        String nombreObjeto = "boletines-masivos/" + trabajoId + "/" + estudianteId + ".pdf";

        try (ByteArrayInputStream is = new ByteArrayInputStream(pdf)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucket)
                            .object(nombreObjeto)
                            .stream(is, pdf.length, -1)
                            .contentType("application/pdf")
                            .build()
            );
        }
    }
}