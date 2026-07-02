package com.sga.unemi.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Configuration;

/**
 * Habilita el soporte de caché de Spring (RNF-0008: Optimización de
 * Consultas Frecuentes).
 * <p>
 * Usa el proveedor de caché configurado en {@code application.properties}
 * (actualmente {@code spring.cache.type=simple}, caché en memoria). Para
 * un despliegue con múltiples instancias del backend, se puede cambiar a
 * {@code spring.cache.type=redis} y descomentar el bean RedisCacheManager
 * en esta clase.
 */
@Configuration
@EnableCaching
public class CacheConfig {
}