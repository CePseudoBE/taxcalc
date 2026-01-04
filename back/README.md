# Back-Taxes Backend

API REST pour le calcul des taxes automobiles belges (TMC et taxe de circulation annuelle) pour les trois regions: Wallonie, Bruxelles et Flandre.

## Stack Technique

| Technologie | Version |
|-------------|---------|
| Java | 24 |
| Spring Boot | 4.0.0 |
| PostgreSQL | - (production) |
| H2 | - (developpement) |
| Liquibase | - |
| Gradle | 9.x |

## Demarrage Rapide

### Prerequis

- Java 24+
- PostgreSQL (pour la production uniquement)

### Mode Developpement

Utilise H2 en memoire, pas besoin de base de donnees externe:

```bash
./gradlew bootRun --args='--spring.profiles.active=dev'
```

L'API sera disponible sur `http://localhost:8080`

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Console H2**: http://localhost:8080/h2-console

### Mode Production

Necessite PostgreSQL:

```bash
# Configurer les variables d'environnement
export DATABASE_URL=jdbc:postgresql://localhost:5432/backtaxes
export DATABASE_USER=postgres
export DATABASE_PASSWORD=your_password

# Lancer l'application
./gradlew bootRun --args='--spring.profiles.active=prod'
```

### Tests

```bash
./gradlew test
```

## Profils Spring

| Profil | Base de donnees | Securite | CORS |
|--------|-----------------|----------|------|
| `dev` | H2 (memoire) | Desactivee | localhost:3000, localhost:5173 |
| `prod` | PostgreSQL | Activee | backtaxes.be |
| (defaut) | PostgreSQL | Desactivee | localhost:3000, localhost:5173 |

## Configuration

### Variables d'Environnement (Production)

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | URL JDBC PostgreSQL |
| `DATABASE_USER` | Utilisateur base de donnees |
| `DATABASE_PASSWORD` | Mot de passe base de donnees |

### Fichiers de Configuration

| Fichier | Usage |
|---------|-------|
| `application.properties` | Configuration par defaut |
| `application-dev.properties` | Mode developpement (H2) |
| `application-prod.properties` | Mode production (securite activee) |

## API Endpoints

### Calcul de Taxes (Public)

```
POST /api/tax/calculate   # Calcul TMC + taxe annuelle
POST /api/tax/tmc         # Calcul TMC uniquement
POST /api/tax/annual      # Calcul taxe annuelle uniquement
```

### Catalogue Vehicules (Public)

```
GET /api/brands                  # Liste des marques
GET /api/brands/{id}/models      # Modeles d'une marque
GET /api/models/{id}/variants    # Variantes d'un modele
GET /api/variants/{id}           # Details d'une variante
```

### Authentification (Public)

```
POST /api/auth/register   # Inscription
POST /api/auth/login      # Connexion
```

### Utilisateur (Authentifie)

```
GET/POST /api/submissions/**      # Soumissions de vehicules
GET/POST /api/saved-searches/**   # Recherches sauvegardees
```

### Moderation (Role MODERATOR/ADMIN)

```
GET/POST /api/moderation/**   # Gestion des soumissions
```

### Administration (Role ADMIN)

```
GET/POST /api/admin/**   # Gestion des taxes et parametres
```

## Exemple de Requete

### Calcul de TMC

```bash
curl -X POST http://localhost:8080/api/tax/tmc \
  -H "Content-Type: application/json" \
  -d '{
    "region": "wallonia",
    "fiscalHp": 10,
    "co2Wltp": 150,
    "fuel": "petrol",
    "euroNorm": "euro_6",
    "registrationDate": "2024-01-15"
  }'
```

## Structure du Projet

```
src/main/java/be/hoffmann/backtaxes/
├── controller/     # Endpoints REST
├── service/        # Logique metier
├── repository/     # Acces base de donnees
├── entity/         # Entites JPA
├── dto/            # Objets de transfert
├── config/         # Configuration Spring
└── exception/      # Gestion des erreurs

src/main/resources/
├── db/changelog/   # Migrations Liquibase
├── application.properties
├── application-dev.properties
└── application-prod.properties
```

## Documentation API

La documentation OpenAPI est generee automatiquement et accessible via:

| URL | Description |
|-----|-------------|
| `/swagger-ui.html` | Interface Swagger UI interactive |
| `/v3/api-docs` | Specification OpenAPI (JSON) |
| `/v3/api-docs.yaml` | Specification OpenAPI (YAML) |

## Documentation Supplementaire

- `TAX_FORMULAS.md` - Formules de calcul des taxes par region
- `CLAUDE.md` - Instructions pour le developpement
