# Checklist Production - Back-Taxes

Ce document liste toutes les modifications a effectuer avant le deploiement en production.

---

## 1. Securite - SecurityConfig.java

**Fichier:** `src/main/java/be/hoffmann/backtaxes/config/SecurityConfig.java`

**Action:** Decommenter la configuration de securite complete et supprimer le mode dev.

```java
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .csrf(AbstractHttpConfigurer::disable)

        // REMPLACER .anyRequest().permitAll() PAR:
        .authorizeHttpRequests(auth -> auth
            // Endpoints publics - Catalogue et calcul de taxes
            .requestMatchers(HttpMethod.GET, "/api/brands/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/models/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/variants/**").permitAll()
            .requestMatchers("/api/tax/**").permitAll()

            // Endpoints publics - Authentification
            .requestMatchers("/api/auth/**").permitAll()

            // Endpoints moderation - Moderateurs uniquement
            .requestMatchers("/api/moderation/**").hasRole("MODERATOR")

            // Endpoints admin - Administrateurs uniquement
            .requestMatchers("/api/admin/**").hasRole("ADMIN")

            // Tous les autres endpoints necessitent une authentification
            .anyRequest().authenticated()
        )

        // Gestion de session
        .sessionManagement(session -> session
            .maximumSessions(5)
        );

    return http.build();
}
```

**Ne pas oublier:** Decommenter l'import `HttpMethod`.

---

## 2. CORS - WebConfig.java

**Fichier:** `src/main/java/be/hoffmann/backtaxes/config/WebConfig.java`

**Action:** Remplacer les origines localhost par les domaines de production.

```java
@Override
public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
            .allowedOrigins(
                    "https://backtaxes.be",        // Domaine principal
                    "https://www.backtaxes.be"    // Avec www
            )
            .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
            .allowedHeaders("*")
            .allowCredentials(true)
            .maxAge(3600);
}
```

---

## 3. Authentification - Controllers

**Fichiers:**
- `src/main/java/be/hoffmann/backtaxes/controller/SubmissionController.java`
- `src/main/java/be/hoffmann/backtaxes/controller/SavedSearchController.java`

**Action:** Supprimer le fallback vers l'utilisateur dev (ID 1).

**Remplacer:**
```java
private User getCurrentUser(HttpSession session) {
    Long userId = (Long) session.getAttribute(USER_SESSION_KEY);
    if (userId == null) {
        // Mode dev: tenter d'utiliser l'utilisateur par defaut (ID 1)
        try {
            return userService.findById(1L);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifie");
        }
    }
    return userService.findById(userId);
}
```

**Par:**
```java
private User getCurrentUser(HttpSession session) {
    Long userId = (Long) session.getAttribute(USER_SESSION_KEY);
    if (userId == null) {
        throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Utilisateur non authentifie");
    }
    return userService.findById(userId);
}
```

---

## 4. Base de donnees - Credentials

**Fichier:** `src/main/resources/application.properties`

**Action:** Utiliser des variables d'environnement au lieu de credentials en dur.

**Remplacer:**
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/backtaxes
spring.datasource.username=postgres
spring.datasource.password=password
```

**Par:**
```properties
spring.datasource.url=${DATABASE_URL}
spring.datasource.username=${DATABASE_USERNAME}
spring.datasource.password=${DATABASE_PASSWORD}
```

**Ou utiliser Spring Cloud Config / Vault pour les secrets.**

---

## 5. Utilisateur dev - Migration

**Fichier:** `src/main/resources/db/changelog/changes/019-seed-dev-user.yaml`

**Action:** Supprimer ce fichier ou le desactiver en production.

**Option A - Supprimer le fichier:**
```bash
rm src/main/resources/db/changelog/changes/019-seed-dev-user.yaml
```

**Option B - Ajouter un contexte Liquibase:**
```yaml
databaseChangeLog:
  - changeSet:
      id: 19
      author: axel
      context: dev  # Ne s'execute qu'en contexte dev
      changes:
        # ...
```

Puis configurer le contexte dans application.properties:
```properties
# Dev
spring.liquibase.contexts=dev

# Prod
spring.liquibase.contexts=prod
```

---

## 6. Role ADMIN - A implementer

**Probleme actuel:** Le role `ADMIN` est requis pour `/api/admin/**` mais il n'existe pas de mecanisme pour l'assigner.

**Solution recommandee:** Ajouter un champ `is_admin` a la table `users`.

**Migration a creer:**
```yaml
databaseChangeLog:
  - changeSet:
      id: 20
      author: axel
      changes:
        - addColumn:
            tableName: users
            columns:
              - column:
                  name: is_admin
                  type: boolean
                  defaultValueBoolean: false
```

**Modifier l'entite User:**
```java
@Column(name = "is_admin")
private Boolean isAdmin = false;
```

**Modifier SecurityConfig pour gerer les roles:**
```java
// Creer un UserDetailsService custom qui mappe isModerator/isAdmin vers ROLE_MODERATOR/ROLE_ADMIN
```

---

## 7. Logging - Configuration

**Fichier:** `src/main/resources/application.properties`

**Action:** Configurer les logs pour la production.

```properties
# Niveau de log en production
logging.level.root=WARN
logging.level.be.hoffmann.backtaxes=INFO

# Logs vers fichier
logging.file.name=/var/log/backtaxes/application.log
logging.file.max-size=10MB
logging.file.max-history=30
```

---

## 8. Sessions - Configuration

**Action:** Configurer Redis ou une base de donnees pour les sessions en production (au lieu de JDBC local).

```properties
# Option Redis
spring.session.store-type=redis
spring.redis.host=${REDIS_HOST}
spring.redis.port=6379

# Duree de session
server.servlet.session.timeout=30m
```

---

## 9. HTTPS - Configuration

**Action:** S'assurer que HTTPS est force en production.

```java
// Dans SecurityConfig.java
http.requiresChannel(channel ->
    channel.anyRequest().requiresSecure()
);
```

Ou via reverse proxy (nginx/Apache).

---

## 10. Validation supplementaire

**Fichier:** `src/main/java/be/hoffmann/backtaxes/dto/request/TaxCalculationRequest.java`

**Action:** Ajouter une validation custom pour le XOR variantId/submissionId.

```java
@AssertTrue(message = "Soit variantId soit submissionId doit etre fourni, pas les deux")
public boolean hasValidVehicleReference() {
    return (variantId != null) != (submissionId != null);
}
```

---

## 11. Rate Limiting - CRITIQUE

**Probleme:** L'API publique `/api/tax/**` n'a pas de rate limiting, ce qui expose le service aux abus et attaques DDoS.

**Solution recommandee:** Utiliser Bucket4j avec Spring Boot.

**Dependance a ajouter dans `build.gradle`:**
```groovy
implementation 'com.bucket4j:bucket4j-core:8.7.0'
implementation 'com.bucket4j:bucket4j-spring-boot-starter:8.7.0'
```

**Configuration dans `application.properties`:**
```properties
# Rate limiting - 100 requetes par minute par IP
bucket4j.enabled=true
bucket4j.filters[0].cache-name=rate-limit-buckets
bucket4j.filters[0].url=/api/tax/.*
bucket4j.filters[0].rate-limits[0].bandwidths[0].capacity=100
bucket4j.filters[0].rate-limits[0].bandwidths[0].time=1
bucket4j.filters[0].rate-limits[0].bandwidths[0].unit=minutes
bucket4j.filters[0].rate-limits[0].bandwidths[0].refill-speed=interval
```

**Alternative - Intercepteur custom:**
```java
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = request.getRemoteAddr();
        Bucket bucket = buckets.computeIfAbsent(ip, this::createBucket);

        if (!bucket.tryConsume(1)) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return false;
        }
        return true;
    }

    private Bucket createBucket(String ip) {
        return Bucket.builder()
            .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
            .build();
    }
}
```

**Headers de reponse a inclure:**
- `X-RateLimit-Limit`: Nombre max de requetes
- `X-RateLimit-Remaining`: Requetes restantes
- `X-RateLimit-Reset`: Timestamp de reset

---

## 12. Headers de securite

**Fichier:** Creer `src/main/java/be/hoffmann/backtaxes/config/SecurityHeadersConfig.java`

**Action:** Ajouter les headers de securite HTTP.

```java
@Configuration
public class SecurityHeadersConfig {

    @Bean
    public FilterRegistrationBean<SecurityHeadersFilter> securityHeadersFilter() {
        FilterRegistrationBean<SecurityHeadersFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new SecurityHeadersFilter());
        registration.addUrlPatterns("/api/*");
        return registration;
    }
}

public class SecurityHeadersFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Empecher le sniffing MIME
        response.setHeader("X-Content-Type-Options", "nosniff");

        // Protection XSS
        response.setHeader("X-XSS-Protection", "1; mode=block");

        // Empecher le clickjacking
        response.setHeader("X-Frame-Options", "DENY");

        // Politique de referrer
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");

        // Politique de permissions
        response.setHeader("Permissions-Policy", "geolocation=(), microphone=(), camera=()");

        filterChain.doFilter(request, response);
    }
}
```

---

## 13. Health Checks / Actuator

**Action:** Activer Spring Boot Actuator pour le monitoring.

**Dependance dans `build.gradle`:**
```groovy
implementation 'org.springframework.boot:spring-boot-starter-actuator'
```

**Configuration dans `application.properties`:**
```properties
# Actuator - Endpoints exposes
management.endpoints.web.exposure.include=health,info,metrics,prometheus
management.endpoint.health.show-details=when_authorized

# Health checks detailles
management.health.db.enabled=true
management.health.diskspace.enabled=true

# Base path pour actuator (securise)
management.endpoints.web.base-path=/internal/actuator
```

**Securiser les endpoints Actuator dans `SecurityConfig.java`:**
```java
.requestMatchers("/internal/actuator/**").hasRole("ADMIN")
```

---

## 14. Monitoring / Metrics (Prometheus)

**Dependance dans `build.gradle`:**
```groovy
implementation 'io.micrometer:micrometer-registry-prometheus'
```

**Configuration dans `application.properties`:**
```properties
management.prometheus.metrics.export.enabled=true
management.metrics.tags.application=back-taxes
```

**Metriques custom pour les calculs de taxes:**
```java
@Service
public class TaxCalculationService {
    private final Counter taxCalculationCounter;
    private final Timer taxCalculationTimer;

    public TaxCalculationService(MeterRegistry registry, ...) {
        this.taxCalculationCounter = Counter.builder("tax.calculations.total")
            .description("Total tax calculations")
            .tag("type", "all")
            .register(registry);
        this.taxCalculationTimer = Timer.builder("tax.calculations.duration")
            .description("Tax calculation duration")
            .register(registry);
    }
}
```

---

## 15. Timeouts et Pool de connexions DB

**Configuration dans `application.properties`:**
```properties
# Pool de connexions HikariCP
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.max-lifetime=1800000

# Validation des connexions
spring.datasource.hikari.validation-timeout=5000
```

---

## 16. Compression des reponses

**Configuration dans `application.properties`:**
```properties
# Compression GZIP
server.compression.enabled=true
server.compression.mime-types=application/json,application/xml,text/html,text/plain
server.compression.min-response-size=1024
```

---

## Resume des fichiers a modifier

| Fichier | Priorite | Action |
|---------|----------|--------|
| `SecurityConfig.java` | CRITIQUE | Reactiver auth |
| `WebConfig.java` | CRITIQUE | Domaines prod |
| `SubmissionController.java` | CRITIQUE | Supprimer fallback dev |
| `SavedSearchController.java` | CRITIQUE | Supprimer fallback dev |
| `application.properties` | CRITIQUE | Variables env pour credentials |
| **Rate Limiting** | **CRITIQUE** | **Ajouter Bucket4j ou intercepteur** |
| `019-seed-dev-user.yaml` | HAUTE | Supprimer ou contextualiser |
| `User.java` + migration | MOYENNE | Ajouter role ADMIN |
| Logging config | MOYENNE | Configurer logs prod |
| Sessions config | MOYENNE | Redis ou DB externe |
| HTTPS | HAUTE | Forcer HTTPS |
| **Headers de securite** | **HAUTE** | **X-Content-Type-Options, etc.** |
| **Actuator** | **HAUTE** | **Health checks + metrics** |
| **Prometheus** | MOYENNE | Metriques custom |
| **HikariCP** | MOYENNE | Pool de connexions |
| **Compression** | BASSE | GZIP pour JSON |

---

## Commandes de verification

```bash
# Compiler
./gradlew build

# Tests
./gradlew test

# Verifier les vulnerabilites (si OWASP plugin)
./gradlew dependencyCheckAnalyze

# Lancer en mode prod local
./gradlew bootRun --args='--spring.profiles.active=prod'
```

---

## Checklist pre-deploiement

- [ ] Rate limiting configure et teste
- [ ] CORS configure avec domaines production
- [ ] Variables d'environnement pour credentials
- [ ] HTTPS force
- [ ] Headers de securite actifs
- [ ] Actuator securise (role ADMIN)
- [ ] Logs configures vers fichier
- [ ] Sessions sur Redis/DB externe
- [ ] Pool de connexions configure
- [ ] Compression activee
- [ ] Fallback dev supprime
- [ ] Utilisateur dev supprime de la migration

---

*Document mis a jour le 03/01/2026*
