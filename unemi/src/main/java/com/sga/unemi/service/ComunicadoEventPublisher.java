package com.sga.unemi.service;

import com.sga.unemi.config.RabbitMQConfig;
import com.sga.unemi.dto.ComunicadoMensaje;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class ComunicadoEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(ComunicadoEventPublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public ComunicadoEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @CircuitBreaker(name = "rabbitmq", fallbackMethod = "publicarFallback")
    public void publicar(ComunicadoMensaje mensaje) {
        rabbitTemplate.convertAndSend(
                RabbitMQConfig.COMUNICADOS_EXCHANGE,
                RabbitMQConfig.COMUNICADOS_ROUTING_KEY,
                mensaje
        );
    }

    public void publicarFallback(ComunicadoMensaje mensaje, Throwable t) {
        log.error("No se pudo publicar el comunicado {} a RabbitMQ. El comunicado quedo guardado " +
                        "pero el envio de notificaciones no se proceso. Causa: {}",
                mensaje.getComunicadoId(), t.getMessage());
    }
}