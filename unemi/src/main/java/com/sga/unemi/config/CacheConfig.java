package com.sga.unemi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

/**
 * Habilita el soporte de caché de Spring con Redis como proveedor
 * (RNF-0008: Optimización de Consultas Frecuentes).
 * <p>
 * Configura el {@code RedisCacheManager} con serialización JSON
 * (Jackson) en vez de la serialización binaria de Java por defecto,
 * para que los datos cacheados sean legibles en la consola de Redis
 * e interoperables con otros clientes. Los valores nulos no se cachean
 * para evitar falsos negativos. El TTL por defecto es 10 minutos.
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /**
     * Configura el administrador de caché de Redis con serialización JSON
     * y un TTL de 10 minutos para todas las entradas.
     *
     * @param connectionFactory la conexión a Redis provista por Spring Boot
     * @return el {@code RedisCacheManager} configurado
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(
                                new GenericJackson2JsonRedisSerializer()
                        )
                );

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}