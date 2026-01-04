# Back-Taxes - Calculateur de Taxes Automobiles Belges

## Description du Projet

Back-Taxes est une application web permettant de calculer les taxes automobiles en Belgique. Elle couvre les trois régions belges (Wallonie, Flandre, Bruxelles) et supporte deux types de taxes :

- **TMC (Taxe de Mise en Circulation)** / **BIV (Belasting op Inverkeerstelling)** pour la Flandre
- **Taxe de circulation annuelle**

## Architecture

Le projet est divisé en deux parties :

```
back-taxes/
├── back/          # API Backend (Java/Spring Boot)
├── front/         # Application Frontend (Nuxt/Vue.js)
└── claude.md      # Ce fichier
```

---

## Backend (`/back`)

### Stack Technique

| Technologie | Version | Usage |
|-------------|---------|-------|
| Java | 24 | Langage |
| Spring Boot | 4.0.0 | Framework |
| Gradle | - | Build tool |
| PostgreSQL | - | Base de données (production) |
| H2 | - | Base de données (développement) |
| Liquibase | - | Migrations de base de données |
| Spring Security | - | Authentification/Autorisation |
| Caffeine | - | Cache en mémoire |

### Structure du Backend

```
src/main/java/be/hoffmann/backtaxes/
├── controller/          # Endpoints REST
│   ├── TaxController.java           # Calcul de taxes
│   ├── BrandController.java         # Marques de véhicules
│   ├── ModelController.java         # Modèles de véhicules
│   ├── VariantController.java       # Variantes de véhicules
│   ├── UserController.java          # Gestion utilisateurs
│   ├── SubmissionController.java    # Soumissions de véhicules
│   ├── SavedSearchController.java   # Recherches sauvegardées
│   └── admin/                       # Endpoints administration
├── service/             # Logique métier
├── entity/              # Entités JPA
├── dto/                 # Objets de transfert (request/response)
├── repository/          # Accès base de données
├── config/              # Configuration Spring
└── exception/           # Gestion des erreurs
```

### Entités Principales

- **Brand** → **Model** → **Variant** : Hiérarchie des véhicules
- **User** : Utilisateurs avec rôles (user, moderator, admin)
- **VehicleSubmission** : Soumissions de nouveaux véhicules par les utilisateurs
- **TaxBracket** / **TaxParameter** : Configuration des tranches et paramètres de taxes
- **AgeCoefficient** : Coefficients de réduction par âge du véhicule
- **SavedSearch** : Recherches sauvegardées par les utilisateurs

### Enums Importants

- **Region** : `wallonia`, `flanders`, `brussels`
- **FuelType** : `petrol`, `diesel`, `electric`, `hybrid_petrol`, `hybrid_diesel`, `lpg`, `cng`, `hydrogen`, etc.
- **EuroNorm** : `euro_1` à `euro_7`
- **TaxType** : `tmc`, `annual`

### Endpoints API Principaux

| Méthode | Endpoint | Description |
|---------|----------|-------------|
| GET | `/api/brands` | Liste des marques |
| GET | `/api/brands/{id}/models` | Modèles d'une marque |
| GET | `/api/models/{id}/variants` | Variantes d'un modèle |
| POST | `/api/tax/calculate` | Calcul TMC + taxe annuelle |
| POST | `/api/tax/tmc` | Calcul TMC uniquement |
| POST | `/api/tax/annual` | Calcul taxe annuelle uniquement |
| POST | `/api/auth/register` | Inscription |
| POST | `/api/auth/login` | Connexion |
| POST | `/api/submissions` | Soumettre un nouveau véhicule |

### Commandes de Développement

```bash
# Mode développement (H2 en mémoire)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Mode production (nécessite PostgreSQL)
./gradlew bootRun

# Lancer les tests
./gradlew test
```

### Configuration CORS

Le backend accepte les origines suivantes :
- `http://localhost:3000`
- `http://localhost:5173`
- `http://localhost:4200`

---

## Frontend (`/front`)

Voir le fichier `front/claude.md` pour les détails spécifiques au frontend.

### Stack Technique

- **Nuxt 4.2.2** avec Vue.js
- **Nuxt UI 4.x** pour les composants
- **TypeScript**
- **Bun** comme package manager

---

## Logique de Calcul des Taxes

### Wallonie & Bruxelles (TMC)

La TMC est calculée selon :
- La puissance fiscale (CV fiscaux)
- L'âge du véhicule (coefficients de réduction)
- Le type de carburant
- Les émissions CO2 (malus écologique)

### Flandre (BIV)

Le BIV utilise une formule différente basée sur :
- Les émissions CO2
- La norme Euro
- L'âge du véhicule
- Le type de carburant

### Taxe Annuelle

Calculée selon :
- La puissance fiscale
- Le type de carburant (diesel vs essence vs électrique)
- La région

### Exemptions

- Véhicules électriques : exemptés jusqu'en 2026
- Véhicules de plus de 25 ans : réductions significatives

---

## Base de Données

### Migrations Liquibase

Les migrations sont dans `back/src/main/resources/db/changelog/changes/` :

- `001-022` : Schéma et données de taxes pour 2025
- Données pré-remplies pour les 3 régions
- Indexations annuelles appliquées

### Enums PostgreSQL

Le projet utilise des types ENUM PostgreSQL natifs pour la sécurité de type.
