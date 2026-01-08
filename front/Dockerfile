# Back-Taxes Frontend - Dockerfile
# Multi-stage build for Nuxt 4 with Bun

# ============================================
# Stage 1: Build
# ============================================
FROM oven/bun:1 AS builder

WORKDIR /app

# Copier les fichiers de dépendances
COPY package.json bun.lock ./

# Installer les dépendances
RUN bun install --frozen-lockfile

# Copier le reste du code
COPY . .

# Build l'application
RUN bun run build

# ============================================
# Stage 2: Runtime
# ============================================
FROM node:22-alpine AS runtime

# Labels
LABEL maintainer="Back-Taxes"
LABEL description="Frontend for Belgian automobile tax calculator"

# Créer un utilisateur non-root
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copier le build output depuis le stage de build
COPY --from=builder /app/.output .output

# Changer le propriétaire
RUN chown -R appuser:appgroup /app

# Utiliser l'utilisateur non-root
USER appuser

# Port exposé
EXPOSE 3000

# Variables d'environnement
ENV NODE_ENV=production
ENV NUXT_HOST=0.0.0.0
ENV NUXT_PORT=3000

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:3000/ || exit 1

# Commande de démarrage
CMD ["node", ".output/server/index.mjs"]
