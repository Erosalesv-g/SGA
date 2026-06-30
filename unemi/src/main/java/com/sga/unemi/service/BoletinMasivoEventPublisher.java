package com.sga.unemi.service;

import com.sga.unemi.config.RabbitMQConfig;
import com.sga.unemi.dto.BoletinMasivoMensaje;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de generación masiva de boletines a RabbitMQ,
 * protegido con el patrón Circuit Breaker (RNF-0011), siguiendo el mismo
 * enfoque que {@link ComunicadoEventPublisher} para el flujo de
 * Comunicados.
 * <p>
 * Si RabbitMQ no está disponible, el trabajo queda registrado en estado
 * PENDIENTE en la base de datos (ver {@code TrabajoBoletinMasivo}) y el
 * fallo se registra en el log, en vez de que la petición HTTP que solicitó
 * la generación masiva falle por completo.
 */
@Component
public class BoletinMasivoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(BoletinMasivoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public BoletinMasivoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica el evento de un trabajo de boletines masivos nuevo a la cola
     * de RabbitMQ para que {@code BoletinMasivoConsumer} lo procese de
     * forma asíncrona.
     *
     * @param mensaje evento con el identificador del trabajo a procesar
     */
    @CircuitBreaker(name = "rabbitmq", fallbackMethod = "publicarFallback")
    public void publicar(BoletinMasivoMensaje mensaje) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.BOLETINES_MASIVOS_EXCHANGE,
                RabbitMQConfig.BOLETINES_MASIVOS_ROUTING_KEY,
                mensaje
        );
    }

    /**
     * Método de fallback invocado automáticamente por Resilience4j cuando
     * {@link #publicar(BoletinMasivoMensaje)} falla. El trabajo permanece
     * en estado PENDIENTE en la base de datos para reintento manual o
     * cuando RabbitMQ vuelva a estar disponible.
     *
     * @param mensaje evento que no se pudo publicar
     * @param t       la causa original del fallo
     */
    public void publicarFallback(BoletinMasivoMensaje mensaje, Throwable t) {
        log.error("No se pudo publicar el trabajo de boletines masivos {} a RabbitMQ. " +
                        "El trabajo quedo registrado pero no se proceso. Causa: {}",
                mensaje.getTrabajoId(), t.getMessage());
    }
}