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
 * Configuración de la infraestructura de mensajería RabbitMQ.
 * <p>
 * Define dos flujos independientes de mensajería asíncrona (RNF-0011):
 * <ul>
 *   <li><b>Comunicados</b> (RF-06): envío masivo de notificaciones a los
 *       destinatarios de un comunicado.</li>
 *   <li><b>Boletines masivos</b> (RF-08): generación en lote de boletines
 *       de calificaciones en PDF para todos los estudiantes de un nivel,
 *       almacenados en MinIO.</li>
 * </ul>
 * Cada flujo tiene su propia cola, exchange y routing key, pero comparten el
 * mismo conversor de mensajes JSON.
 */
@Configuration
public class RabbitMQConfig {

    /** Nombre de la cola donde se encolan los eventos de comunicados nuevos. */
    public static final String COMUNICADOS_QUEUE = "comunicados.queue";

    /** Nombre del exchange (intercambiador) de tipo directo para comunicados. */
    public static final String COMUNICADOS_EXCHANGE = "comunicados.exchange";

    /** Routing key usada para enrutar los mensajes del exchange a la cola. */
    public static final String COMUNICADOS_ROUTING_KEY = "comunicados.nuevo";

    /** Nombre de la cola donde se encolan los trabajos de boletines masivos. */
    public static final String BOLETINES_MASIVOS_QUEUE = "boletines.masivos.queue";

    /** Nombre del exchange de tipo directo para boletines masivos. */
    public static final String BOLETINES_MASIVOS_EXCHANGE = "boletines.masivos.exchange";

    /** Routing key usada para enrutar los mensajes del exchange a la cola de boletines. */
    public static final String BOLETINES_MASIVOS_ROUTING_KEY = "boletines.masivos.nuevo";

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
     * Declara la cola de boletines masivos como durable. Los trabajos
     * pueden tardar varios segundos en procesarse (cientos de PDFs), por lo
     * que es importante que no se pierdan si RabbitMQ se reinicia mientras
     * hay trabajos pendientes en la cola.
     */
    @Bean
    public Queue boletinesMasivosQueue() {
        return new Queue(BOLETINES_MASIVOS_QUEUE, true);
    }

    /**
     * Declara el exchange directo al que se publican los trabajos de
     * generación masiva de boletines.
     */
    @Bean
    public DirectExchange boletinesMasivosExchange() {
        return new DirectExchange(BOLETINES_MASIVOS_EXCHANGE);
    }

    /**
     * Enlaza la cola de boletines masivos con su exchange mediante la
     * routing key definida.
     */
    @Bean
    public Binding boletinesMasivosBinding(Queue boletinesMasivosQueue, DirectExchange boletinesMasivosExchange) {
        return BindingBuilder.bind(boletinesMasivosQueue)
                .to(boletinesMasivosExchange)
                .with(BOLETINES_MASIVOS_ROUTING_KEY);
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