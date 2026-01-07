package be.hoffmann.backtaxes.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

/**
 * Configuration du cache avec Caffeine.
 *
 * Les donnees de taxes (brackets, parameters, coefficients) changent rarement
 * (au plus une fois par an lors des indexations), donc on peut les cacher longtemps.
 *
 * Configuration via application.properties:
 * - app.cache.ttl-hours: duree de vie en heures (defaut: 24)
 * - app.cache.max-size: nombre max d'entrees (defaut: 1000)
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Value("${app.cache.ttl-hours:24}")
    private int ttlHours;

    @Value("${app.cache.max-size:1000}")
    private int maxSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Configuration avec TTL et taille max configurables
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(ttlHours, TimeUnit.HOURS)
                .maximumSize(maxSize)
                .recordStats()); // Pour le monitoring via actuator

        // Caches specifiques
        cacheManager.setCacheNames(java.util.List.of(
                "taxBrackets",      // Tranches de taxes
                "taxParameters",    // Parametres (CO2 ref, MMA ref, etc.)
                "ageCoefficients",  // Coefficients d'age
                "taxExemptions",    // Exemptions (electrique, hydrogene)
                "minMaxAmounts"     // Montants min/max
        ));

        return cacheManager;
    }
}
