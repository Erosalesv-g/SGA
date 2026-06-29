package com.sga.unemi.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuración de la infraestructura de mensajería RabbitMQ usada para el
 * envío asíncrono de comunicados masivos (RF-06, RNF-0011).
 * <p>
 * Define la cola, el exchange y el binding entre ambos, además del
 * conversor de mensajes a formato JSON para que los objetos Java se
 * serialicen de forma legible en la cola.
 */
@Configuration
public class RabbitMQConfig {

    /** Nombre de la cola donde se encolan los eventos de comunicados nuevos. */
    public static final String COMUNICADOS_QUEUE = "comunicados.queue";

    /** Nombre del exchange (intercambiador) de tipo directo para comunicados. */
    public static final String COMUNICADOS_EXCHANGE = "comunicados.exchange";

    /** Routing key usada para enrutar los mensajes del exchange a la cola. */
    public static final String COMUNICADOS_ROUTING_KEY = "comunicados.nuevo";

    /**
     * Declara la cola de comunicados como durable, es decir, que sus
     * mensajes sobreviven a un reinicio del broker de RabbitMQ.
     */
    @Bean
    public Queue comunicadosQueue() {
        return new Queue(COMUNICADOS_QUEUE, true); // durable: sobrevive si RabbitMQ se reinicia
    }

    /**
     * Declara el exchange directo al que se publican los eventos de
     * comunicados antes de ser enrutados a la cola correspondiente.
     */
    @Bean
    public DirectExchange comunicadosExchange() {
        return new DirectExchange(COMUNICADOS_EXCHANGE);
    }

    /**
     * Enlaza la cola de comunicados con el exchange mediante la routing key
     * definida, completando el circuito de publicación-consumo.
     */
    @Bean
    public Binding comunicadosBinding(Queue comunicadosQueue, DirectExchange comunicadosExchange) {
        return BindingBuilder.bind(comunicadosQueue)
                .to(comunicadosExchange)
                .with(COMUNICADOS_ROUTING_KEY);
    }

    /**
     * Configura la serialización de los mensajes en formato JSON, en lugar
     * del formato binario de serialización de Java por defecto, para que
     * los mensajes sean legibles e interoperables (por ejemplo, visibles en
     * la consola de administración de RabbitMQ).
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}