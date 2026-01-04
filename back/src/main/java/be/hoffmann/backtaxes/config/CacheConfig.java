package be.hoffmann.backtaxes.config;

import com.github.benmanes.caffeine.cache.Caffeine;
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
 */
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();

        // Configuration par defaut: 24h TTL, max 1000 entrees
        cacheManager.setCaffeine(Caffeine.newBuilder()
                .expireAfterWrite(24, TimeUnit.HOURS)
                .maximumSize(1000)
                .recordStats()); // Pour le monitoring

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
