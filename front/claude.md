# Back-Taxes Frontend

## Package Manager

**Utiliser Bun** pour la gestion des dépendances et l'exécution des scripts.

```bash
# Installation des dépendances
bun install

# Lancer en développement
bun run dev

# Build de production
bun run build

# Preview du build
bun run preview

# Vérification des types
bun run typecheck

# Linting
bun run lint
```

---

## Description du Projet

Interface utilisateur pour le calculateur de taxes automobiles belges. L'application permet aux utilisateurs de :

1. **Calculer les taxes** de mise en circulation (TMC/BIV) et annuelles pour les véhicules en Belgique
2. **Rechercher des véhicules** par marque, modèle et variante
3. **Comparer les taxes** entre les trois régions (Wallonie, Flandre, Bruxelles)
4. **Sauvegarder des recherches** pour consultation ultérieure (utilisateurs connectés)
5. **Soumettre de nouveaux véhicules** non présents dans la base de données

---

## Stack Technique

| Technologie | Version | Usage |
|-------------|---------|-------|
| Nuxt | 4.2.2 | Framework Vue.js |
| Vue.js | 3.x | Framework UI |
| Nuxt UI | 4.x | Composants UI |
| TypeScript | 5.x | Typage |
| Bun | - | Package manager & runtime |

### Modules Nuxt Configurés

- `@nuxt/ui` : Composants UI (basé sur Tailwind)
- `@nuxt/eslint` : Linting
- `@nuxt/hints` : Optimisations de performance
- `@nuxt/image` : Optimisation des images
- `@nuxt/test-utils` : Utilitaires de test

---

## Structure du Projet

```
front/
├── app/
│   ├── app.vue              # Composant racine
│   ├── app.config.ts        # Configuration app
│   ├── assets/css/          # Styles globaux
│   ├── components/          # Composants réutilisables
│   └── pages/               # Pages (routing automatique)
├── public/                  # Assets statiques
├── nuxt.config.ts           # Configuration Nuxt
└── package.json             # Dépendances
```

---

## Fonctionnalités à Implémenter

### Pages Principales

1. **Page d'accueil** (`/`)
   - Formulaire de calcul de taxe
   - Sélection région, marque, modèle, variante
   - Affichage des résultats (TMC + taxe annuelle)

2. **Recherche de véhicules** (`/search`)
   - Recherche par marque/modèle
   - Filtres (type carburant, puissance, etc.)
   - Liste des variantes avec aperçu des taxes

3. **Comparaison régionale** (`/compare`)
   - Comparer les taxes d'un véhicule entre les 3 régions
   - Visualisation graphique des différences

4. **Authentification** (`/auth/login`, `/auth/register`)
   - Connexion / Inscription
   - Gestion de session

5. **Espace utilisateur** (`/account`)
   - Recherches sauvegardées
   - Historique des calculs
   - Soumission de véhicules

6. **Administration** (`/admin`) - Rôle admin/moderator
   - Modération des soumissions de véhicules
   - Gestion des paramètres de taxes

### Composants à Créer

- `TaxCalculator.vue` : Formulaire principal de calcul
- `VehicleSelector.vue` : Sélection marque/modèle/variante en cascade
- `TaxResult.vue` : Affichage des résultats de calcul
- `RegionSelector.vue` : Sélection de la région
- `VehicleCard.vue` : Carte de présentation d'un véhicule
- `SavedSearchList.vue` : Liste des recherches sauvegardées
- `SubmissionForm.vue` : Formulaire de soumission de véhicule

### Stores/Composables

- `useAuth` : Gestion de l'authentification
- `useTax` : Appels API pour les calculs de taxes
- `useVehicles` : Gestion des données véhicules
- `useSavedSearches` : Recherches sauvegardées

---

## API Backend

L'API est accessible sur `http://localhost:8080/api/`.

### Endpoints Principaux

```typescript
// Véhicules
GET  /api/brands                    // Liste des marques
GET  /api/brands/{id}/models        // Modèles d'une marque
GET  /api/models/{id}/variants      // Variantes d'un modèle
GET  /api/variants/{id}             // Détails d'une variante

// Calcul de taxes
POST /api/tax/calculate             // Calcul complet (TMC + annuel)
POST /api/tax/tmc                   // TMC uniquement
POST /api/tax/annual                // Taxe annuelle uniquement

// Authentification
POST /api/auth/register             // Inscription
POST /api/auth/login                // Connexion
POST /api/auth/logout               // Déconnexion
GET  /api/auth/check                // Vérifier la session

// Utilisateur
GET  /api/users/me                  // Profil utilisateur
GET  /api/saved-searches            // Recherches sauvegardées
POST /api/saved-searches            // Sauvegarder une recherche
POST /api/submissions               // Soumettre un véhicule
```

### Types Importants

```typescript
type Region = 'wallonia' | 'flanders' | 'brussels'

type FuelType =
  | 'petrol'
  | 'diesel'
  | 'electric'
  | 'hybrid_petrol'
  | 'hybrid_diesel'
  | 'lpg'
  | 'cng'
  | 'hydrogen'

type EuroNorm = 'euro_1' | 'euro_2' | 'euro_3' | 'euro_4' | 'euro_5' | 'euro_5b' | 'euro_6' | 'euro_6d' | 'euro_7'

interface TaxCalculationRequest {
  region: Region
  variantId?: number
  powerKw?: number
  fiscalHp?: number
  co2Emission?: number
  fuelType?: FuelType
  euroNorm?: EuroNorm
  firstRegistrationDate?: string  // ISO date
  massKg?: number
}

interface TaxCalculationResponse {
  tmc: number
  annualTax: number
  tmcDetails: TaxDetails
  annualTaxDetails: TaxDetails
}
```

---

## Configuration

### Variables d'environnement

```bash
# .env
NUXT_PUBLIC_API_BASE_URL=http://localhost:8080/api
```

### CORS

Le backend accepte les requêtes depuis :
- `http://localhost:3000` (Nuxt dev)
- `http://localhost:5173` (Vite)
- `http://localhost:4200` (Angular - legacy)

---

## Conventions de Code

- Utiliser **Composition API** avec `<script setup>`
- Nommer les composants en **PascalCase**
- Utiliser **TypeScript** pour tout le code
- Suivre les règles ESLint configurées (pas de trailing comma, braces 1tbs)
