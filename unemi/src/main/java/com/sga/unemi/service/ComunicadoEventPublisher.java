package com.sga.unemi.service;

import com.sga.unemi.config.RabbitMQConfig;
import com.sga.unemi.dto.ComunicadoMensaje;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publicador de eventos de comunicados a RabbitMQ, protegido con el patrón
 * Circuit Breaker (vía Resilience4j), conforme al RNF-0011 (Escalabilidad
 * de Usuarios).
 * <p>
 * El envío masivo de notificaciones de un comunicado se delega a una cola
 * de mensajería para no bloquear la petición HTTP que lo crea. Si RabbitMQ
 * no está disponible (caído, sobrecargado, etc.), el Circuit Breaker evita
 * que los reintentos de conexión se acumulen y tumben la petición original:
 * el comunicado ya quedó guardado en la base de datos antes de llegar aquí,
 * así que el sistema sigue funcionando con degradación controlada en vez de
 * fallar por completo.
 *
 * @see com.sga.unemi.consumer.ComunicadoConsumer
 */
@Component
public class ComunicadoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ComunicadoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public ComunicadoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    /**
     * Publica el evento de un comunicado nuevo a la cola de RabbitMQ para
     * que {@code ComunicadoConsumer} procese el envío masivo de
     * notificaciones de forma asíncrona.
     * <p>
     * Protegido por Resilience4j: si la conexión a RabbitMQ falla, se invoca
     * automáticamente {@link #publicarFallback(ComunicadoMensaje, Throwable)}
     * en lugar de propagar la excepción.
     *
     * @param mensaje evento con el identificador del comunicado a publicar
     */
    @CircuitBreaker(name = "rabbitmq", fallbackMethod = "publicarFallback")
    public void publicar(ComunicadoMensaje mensaje) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.COMUNICADOS_EXCHANGE,
                RabbitMQConfig.COMUNICADOS_ROUTING_KEY,
                mensaje
        );
    }

    /**
     * Método de fallback invocado automáticamente por Resilience4j cuando
     * {@link #publicar(ComunicadoMensaje)} falla (por ejemplo, por que
     * RabbitMQ no responde).
     * <p>
     * No relanza la excepción: el comunicado ya se guardó exitosamente en la
     * base de datos antes de llegar aquí, así que solo se registra el fallo
     * en el log para que el envío de notificaciones se pueda reintentar
     * manualmente más adelante.
     *
     * @param mensaje evento que no se pudo publicar
     * @param t       la causa original del fallo (excepción de conexión, etc.)
     */
    public void publicarFallback(ComunicadoMensaje mensaje, Throwable t) {
        log.error("No se pudo publicar el comunicado {} a RabbitMQ. El comunicado quedo guardado " +
                        "pero el envio de notificaciones no se proceso. Causa: {}",
                mensaje.getComunicadoId(), t.getMessage());
    }
}