package com.sga.unemi.config;
 
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
 
@Configuration
public class RabbitMQConfig {
 
    public static final String COMUNICADOS_QUEUE = "comunicados.queue";
    public static final String COMUNICADOS_EXCHANGE = "comunicados.exchange";
    public static final String COMUNICADOS_ROUTING_KEY = "comunicados.nuevo";
 
    @Bean
    public Queue comunicadosQueue() {
        return new Queue(COMUNICADOS_QUEUE, true); // durable: sobrevive si RabbitMQ se reinicia
    }
 
    @Bean
    public DirectExchange comunicadosExchange() {
        return new DirectExchange(COMUNICADOS_EXCHANGE);
    }
 
    @Bean
    public Binding comunicadosBinding(Queue comunicadosQueue, DirectExchange comunicadosExchange) {
        return BindingBuilder.bind(comunicadosQueue)
                .to(comunicadosExchange)
                .with(COMUNICADOS_ROUTING_KEY);
    }
 
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}