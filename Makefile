# Back-Taxes - Makefile
# Commandes pour le développement et la production

.PHONY: dev dev-back dev-front build test clean docker-build docker-up docker-down docker-logs docker-clean help

# Variables
BACKEND_DIR := back
FRONTEND_DIR := front
DOCKER_COMPOSE := docker compose

# Couleurs pour les messages
CYAN := \033[36m
GREEN := \033[32m
YELLOW := \033[33m
RESET := \033[0m

# ============================================
# DÉVELOPPEMENT
# ============================================

## Lancer backend et frontend en développement
dev:
	@echo "$(CYAN)Lancement du backend et frontend en parallèle...$(RESET)"
	@$(MAKE) -j2 dev-back dev-front

## Lancer uniquement le backend (Spring Boot avec H2)
dev-back:
	@echo "$(GREEN)Démarrage du backend sur http://localhost:8080$(RESET)"
	@cd $(BACKEND_DIR) && ./gradlew bootRun --args='--spring.profiles.active=dev' --console=plain

## Lancer uniquement le frontend (Nuxt dev server)
dev-front:
	@echo "$(GREEN)Démarrage du frontend sur http://localhost:3000$(RESET)"
	@cd $(FRONTEND_DIR) && bun run dev

# ============================================
# BUILD
# ============================================

## Build les deux projets
build: build-back build-front

## Build le backend
build-back:
	@echo "$(CYAN)Build du backend...$(RESET)"
	@cd $(BACKEND_DIR) && ./gradlew build -x test

## Build le frontend
build-front:
	@echo "$(CYAN)Build du frontend...$(RESET)"
	@cd $(FRONTEND_DIR) && bun run build

# ============================================
# TESTS
# ============================================

## Lancer tous les tests
test: test-back test-front

## Tests backend
test-back:
	@echo "$(CYAN)Tests backend...$(RESET)"
	@cd $(BACKEND_DIR) && ./gradlew test

## Tests frontend (typecheck)
test-front:
	@echo "$(CYAN)Typecheck frontend...$(RESET)"
	@cd $(FRONTEND_DIR) && bun run typecheck

## Linting frontend
lint:
	@echo "$(CYAN)Linting frontend...$(RESET)"
	@cd $(FRONTEND_DIR) && bun run lint

# ============================================
# DOCKER - PRODUCTION
# ============================================

## Build les images Docker
docker-build:
	@echo "$(CYAN)Build des images Docker...$(RESET)"
	$(DOCKER_COMPOSE) build

## Démarrer les services Docker (détaché)
docker-up:
	@echo "$(GREEN)Démarrage des services Docker...$(RESET)"
	$(DOCKER_COMPOSE) up -d
	@echo "$(GREEN)Services démarrés:$(RESET)"
	@echo "  - Frontend: http://localhost:3000"
	@echo "  - Backend:  http://localhost:8080"
	@echo "  - PostgreSQL: localhost:5432"

## Démarrer avec rebuild
docker-up-build:
	@echo "$(CYAN)Build et démarrage des services Docker...$(RESET)"
	$(DOCKER_COMPOSE) up -d --build

## Arrêter les services Docker
docker-down:
	@echo "$(YELLOW)Arrêt des services Docker...$(RESET)"
	$(DOCKER_COMPOSE) down

## Afficher les logs Docker
docker-logs:
	$(DOCKER_COMPOSE) logs -f

## Logs backend uniquement
docker-logs-back:
	$(DOCKER_COMPOSE) logs -f backend

## Logs frontend uniquement
docker-logs-front:
	$(DOCKER_COMPOSE) logs -f frontend

## Statut des conteneurs
docker-status:
	$(DOCKER_COMPOSE) ps

## Nettoyer les conteneurs et volumes Docker
docker-clean:
	@echo "$(YELLOW)Nettoyage Docker (conteneurs, images, volumes)...$(RESET)"
	$(DOCKER_COMPOSE) down -v --rmi local

## Redémarrer un service spécifique
docker-restart-%:
	$(DOCKER_COMPOSE) restart $*

# ============================================
# UTILITAIRES
# ============================================

## Nettoyer les builds
clean:
	@echo "$(YELLOW)Nettoyage des builds...$(RESET)"
	@cd $(BACKEND_DIR) && ./gradlew clean
	@rm -rf $(FRONTEND_DIR)/.nuxt $(FRONTEND_DIR)/.output $(FRONTEND_DIR)/node_modules/.cache

## Installer les dépendances
install:
	@echo "$(CYAN)Installation des dépendances...$(RESET)"
	@cd $(FRONTEND_DIR) && bun install

## Initialiser la base de données (dev)
db-init:
	@echo "$(CYAN)Initialisation de la base H2 (dev)...$(RESET)"
	@cd $(BACKEND_DIR) && ./gradlew bootRun --args='--spring.profiles.active=dev' &
	@sleep 10 && kill $$!

## Afficher l'aide
help:
	@echo "$(CYAN)Back-Taxes - Commandes disponibles$(RESET)"
	@echo ""
	@echo "$(GREEN)Développement:$(RESET)"
	@echo "  make dev          - Lancer backend + frontend"
	@echo "  make dev-back     - Lancer uniquement le backend"
	@echo "  make dev-front    - Lancer uniquement le frontend"
	@echo ""
	@echo "$(GREEN)Build:$(RESET)"
	@echo "  make build        - Build les deux projets"
	@echo "  make build-back   - Build le backend"
	@echo "  make build-front  - Build le frontend"
	@echo ""
	@echo "$(GREEN)Tests:$(RESET)"
	@echo "  make test         - Lancer tous les tests"
	@echo "  make test-back    - Tests backend"
	@echo "  make test-front   - Typecheck frontend"
	@echo "  make lint         - Linting frontend"
	@echo ""
	@echo "$(GREEN)Docker (Production):$(RESET)"
	@echo "  make docker-build - Build les images"
	@echo "  make docker-up    - Démarrer les services"
	@echo "  make docker-down  - Arrêter les services"
	@echo "  make docker-logs  - Afficher les logs"
	@echo "  make docker-clean - Nettoyer conteneurs/volumes"
	@echo ""
	@echo "$(GREEN)Utilitaires:$(RESET)"
	@echo "  make clean        - Nettoyer les builds"
	@echo "  make install      - Installer les dépendances"
	@echo "  make help         - Afficher cette aide"

# Commande par défaut
.DEFAULT_GOAL := help
