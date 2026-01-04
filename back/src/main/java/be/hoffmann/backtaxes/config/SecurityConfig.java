package be.hoffmann.backtaxes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuration de la securite Spring Security.
 * Le mode est controle via app.security.enabled dans application.properties.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.security.enabled:false}")
    private boolean securityEnabled;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Desactiver CSRF pour l'API REST
            .csrf(AbstractHttpConfigurer::disable);

        if (securityEnabled) {
            // Mode production: securite activee
            http.authorizeHttpRequests(auth -> auth
                // OpenAPI / Swagger UI
                .requestMatchers("/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()

                // Endpoints publics - Catalogue et calcul de taxes
                .requestMatchers(HttpMethod.GET, "/api/brands/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/models/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/variants/**").permitAll()
                .requestMatchers("/api/tax/**").permitAll()

                // Endpoints publics - Authentification
                .requestMatchers("/api/auth/**").permitAll()

                // Endpoints utilisateur - Soumissions et recherches sauvegardees
                .requestMatchers("/api/submissions/**").authenticated()
                .requestMatchers("/api/saved-searches/**").authenticated()

                // Endpoints moderation - Moderateurs uniquement
                .requestMatchers("/api/moderation/**").hasAnyRole("MODERATOR", "ADMIN")

                // Endpoints admin - Administrateurs uniquement
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // Tous les autres endpoints necessitent une authentification
                .anyRequest().authenticated()
            );
        } else {
            // Mode developpement: tout est accessible
            http.authorizeHttpRequests(auth -> auth
                .anyRequest().permitAll()
            );
        }

        return http.build();
    }
}
