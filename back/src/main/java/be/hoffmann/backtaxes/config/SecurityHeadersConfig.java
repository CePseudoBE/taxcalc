package be.hoffmann.backtaxes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Configuration des headers de securite HTTP.
 *
 * Ajoute les headers de securite recommandes pour proteger contre:
 * - Clickjacking (X-Frame-Options)
 * - MIME type sniffing (X-Content-Type-Options)
 * - XSS attacks (X-XSS-Protection)
 * - Information leakage via referrer (Referrer-Policy)
 */
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public OncePerRequestFilter securityHeadersFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain filterChain)
                    throws ServletException, IOException {

                // Prevent MIME type sniffing
                response.setHeader("X-Content-Type-Options", "nosniff");

                // Prevent clickjacking
                response.setHeader("X-Frame-Options", "DENY");

                // XSS protection (legacy, but still useful for older browsers)
                response.setHeader("X-XSS-Protection", "1; mode=block");

                // Control referrer information
                response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

                // Prevent caching of sensitive data
                response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
                response.setHeader("Pragma", "no-cache");

                filterChain.doFilter(request, response);
            }
        };
    }
}
