package be.hoffmann.backtaxes.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Rate limiting filter using Bucket4j.
 * Protects against brute force attacks and DoS.
 *
 * Uses Redis for distributed rate limiting in production (when Redis is available).
 * Falls back to in-memory storage for development.
 *
 * Limits:
 * - Auth endpoints (/api/auth/*): 10 requests per minute per IP
 * - General API: 100 requests per minute per IP
 */
@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final int AUTH_LIMIT = 10;
    private static final int GENERAL_LIMIT = 100;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    // In-memory fallback for development
    private final Map<String, Bucket> authBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> generalBuckets = new ConcurrentHashMap<>();

    // Redis template (optional - null if Redis not configured)
    private final StringRedisTemplate redisTemplate;
    private final boolean useRedis;

    @Autowired
    public RateLimitingFilter(@Autowired(required = false) StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.useRedis = redisTemplate != null && isRedisAvailable(redisTemplate);
        if (this.useRedis) {
            logger.info("Rate limiting: Using Redis for distributed storage");
        } else {
            logger.info("Rate limiting: Using in-memory storage (development mode)");
        }
    }

    private boolean isRedisAvailable(StringRedisTemplate template) {
        try {
            template.getConnectionFactory().getConnection().ping();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // Auth endpoints: stricter limit (10 req/min) to prevent brute force
    private Bucket createAuthBucket() {
        Bandwidth limit = Bandwidth.classic(AUTH_LIMIT, Refill.greedy(AUTH_LIMIT, WINDOW));
        return Bucket.builder().addLimit(limit).build();
    }

    // General API: standard limit (100 req/min)
    private Bucket createGeneralBucket() {
        Bandwidth limit = Bandwidth.classic(GENERAL_LIMIT, Refill.greedy(GENERAL_LIMIT, WINDOW));
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String clientIp = getClientIP(request);
        String path = request.getRequestURI();

        // Skip rate limiting for health checks and static resources
        if (path.startsWith("/actuator") || path.startsWith("/swagger") || path.startsWith("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        boolean isAuthEndpoint = path.startsWith("/api/auth");

        if (path.startsWith("/api")) {
            boolean allowed;
            long remaining;

            if (useRedis) {
                // Use Redis for distributed rate limiting
                var result = checkRateLimitRedis(clientIp, isAuthEndpoint);
                allowed = result.allowed;
                remaining = result.remaining;
            } else {
                // Use in-memory buckets
                Bucket bucket;
                if (isAuthEndpoint) {
                    bucket = authBuckets.computeIfAbsent(clientIp, k -> createAuthBucket());
                } else {
                    bucket = generalBuckets.computeIfAbsent(clientIp, k -> createGeneralBucket());
                }
                allowed = bucket.tryConsume(1);
                remaining = bucket.getAvailableTokens();
            }

            // Add rate limit headers
            response.addHeader("X-Rate-Limit-Remaining", String.valueOf(remaining));
            response.addHeader("X-Rate-Limit-Limit", String.valueOf(isAuthEndpoint ? AUTH_LIMIT : GENERAL_LIMIT));

            if (!allowed) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.getWriter().write("{\"error\":\"Too many requests. Please try again later.\",\"code\":\"RATE_LIMIT_EXCEEDED\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    /**
     * Check rate limit using Redis.
     * Uses a simple sliding window counter with TTL.
     */
    private RateLimitResult checkRateLimitRedis(String clientIp, boolean isAuthEndpoint) {
        String key = "ratelimit:" + (isAuthEndpoint ? "auth:" : "general:") + clientIp;
        int limit = isAuthEndpoint ? AUTH_LIMIT : GENERAL_LIMIT;

        try {
            // Increment counter
            Long count = redisTemplate.opsForValue().increment(key);
            if (count == null) {
                count = 1L;
            }

            // Set TTL on first request
            if (count == 1) {
                redisTemplate.expire(key, WINDOW);
            }

            long remaining = Math.max(0, limit - count);
            return new RateLimitResult(count <= limit, remaining);
        } catch (Exception e) {
            // Redis error - allow request but log warning
            logger.warn("Redis rate limit check failed, allowing request: " + e.getMessage());
            return new RateLimitResult(true, limit);
        }
    }

    private record RateLimitResult(boolean allowed, long remaining) {}

    /**
     * Get client IP address, considering proxy headers.
     */
    private String getClientIP(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // Take the first IP in the chain (original client)
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}
