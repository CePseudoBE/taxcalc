# Back-Taxes Backend - Dockerfile
# Multi-stage build for Spring Boot with Java 24

# ============================================
# Stage 1: Build
# ============================================
FROM eclipse-temurin:24-jdk-alpine AS builder

WORKDIR /app

# Copier les fichiers Gradle
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Rendre gradlew exécutable
RUN chmod +x gradlew

# Télécharger les dépendances (cache layer)
RUN ./gradlew dependencies --no-daemon || true

# Copier le code source
COPY src src

# Build l'application (sans tests)
RUN ./gradlew bootJar --no-daemon -x test

# ============================================
# Stage 2: Runtime
# ============================================
FROM eclipse-temurin:24-jre-alpine AS runtime

# Labels
LABEL maintainer="Back-Taxes"
LABEL description="Backend API for Belgian automobile tax calculator"

# Créer un utilisateur non-root pour la sécurité
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copier le JAR depuis le stage de build
COPY --from=builder /app/build/libs/*.jar app.jar

# Changer le propriétaire
RUN chown -R appuser:appgroup /app

# Utiliser l'utilisateur non-root
USER appuser

# Port exposé
EXPOSE 8080

# Variables d'environnement par défaut
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=prod

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

# Commande de démarrage
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
