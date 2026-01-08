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

---

## TODO Frontend - Tâches Restantes

### 🔴 HAUTE - Pages à implémenter

| Page | Fichier | État | Action |
|------|---------|------|--------|
| Recherche | `pages/search.vue` | Skeleton | Connecter filtres, API recherche, pagination |
| Comparaison | `pages/compare.vue` | Skeleton | Sélecteur véhicule, calcul 3 régions en parallèle |
| Soumission | `pages/submit.vue` | Skeleton | Formulaire complet, validation, appel API |
| Compte | `pages/account.vue` | Partiel | Charger données réelles (email, date, recherches) |
| Admin Modération | `pages/admin/submissions.vue` | Skeleton | Liste soumissions, boutons approuver/rejeter |
| Admin Dashboard | `pages/admin/index.vue` | Skeleton | Stats réelles depuis API |

### 🔴 HAUTE - Composables à créer

| Composable | Description |
|------------|-------------|
| `useSavedSearches.ts` | `fetchSavedSearches()`, `saveSearch()`, `deleteSavedSearch()` |
| `useSubmissions.ts` | `fetchMySubmissions()`, `createSubmission()`, `approveSubmission()`, `rejectSubmission()` |
| `useUser.ts` | `fetchUserProfile()`, `updateProfile()`, `deleteAccount()` |

### 🔴 HAUTE - Routes serveur à créer

| Route | Description |
|-------|-------------|
| `GET /api/auth/user` | Récupérer profil utilisateur |
| `POST /api/saved-searches` | Créer recherche sauvegardée |
| `GET /api/saved-searches` | Lister recherches sauvegardées |
| `DELETE /api/saved-searches/:id` | Supprimer recherche |
| `POST /api/submissions` | Créer soumission |
| `GET /api/submissions/my` | Mes soumissions |
| `GET /api/admin/submissions` | Toutes les soumissions (admin) |
| `PUT /api/admin/submissions/:id/approve` | Approuver |
| `PUT /api/admin/submissions/:id/reject` | Rejeter |

### 🔴 HAUTE - Bugs à corriger

| Bug | Fichier | Ligne | Correction |
|-----|---------|-------|------------|
| Liens régions cassés | `pages/index.vue` | 71-132 | Changer `to="/"` → `to="/calculator?region=..."` |
| Email hardcodé | `pages/account.vue` | 25 | Utiliser `auth.user.value?.email` |
| Date hardcodée | `pages/account.vue` | 96 | Formater `auth.user.value?.createdAt` |
| Bouton logout inactif | `pages/account.vue` | 31 | Ajouter `@click="handleLogout"` |
| Locale hardcodée | `pages/calculator.vue` | 98 | Utiliser `useI18n()` pour la locale |
| Boutons Save/Share | `pages/calculator.vue` | - | Implémenter fonctionnalité |

### 🟡 MOYENNE

| Tâche | Description |
|-------|-------------|
| Tests unitaires composables | Créer `__tests__/composables/*.spec.ts` |
| Tests pages | Créer `__tests__/pages/*.spec.ts` |
| Validation formulaires | Ajouter validation stricte sur `submit.vue`, `search.vue` |
| Mot de passe oublié | Créer page et flow de récupération |
| Indicateurs chargement | Spinners/skeletons pendant les appels API |
| Améliorer useAuth | Ajouter `changePassword()`, `resetPassword()`, `isAdmin` |

### 🟢 BASSE

| Tâche | Description |
|-------|-------------|
| Accessibilité | Attributs `aria-label`, `role`, contraste |
| Tests responsive | Vérifier mobile/tablette/desktop |
| Configuration Vitest | Créer `vitest.config.ts` |
| `.env.example` | Documenter les variables d'environnement |

### État actuel des pages

| Page | État | Fonctionnel |
|------|------|-------------|
| `/` (index) | ✅ Complet | ⚠️ Liens à corriger |
| `/calculator` | ✅ Complet | ✅ Oui |
| `/auth/login` | ✅ Complet | ✅ Oui |
| `/auth/register` | ✅ Complet | ✅ Oui |
| `/search` | 🔲 Skeleton | ❌ Non |
| `/compare` | 🔲 Skeleton | ❌ Non |
| `/submit` | 🔲 Skeleton | ❌ Non |
| `/account` | ⚠️ Partiel | ❌ Non |
| `/admin` | 🔲 Skeleton | ❌ Non |
| `/admin/submissions` | 🔲 Skeleton | ❌ Non |

### Composables existants

| Composable | État | Notes |
|------------|------|-------|
| `useAuth.ts` | ✅ 90% | Manque `isAdmin`, refresh token |
| `useApi.ts` | ✅ Complet | OK |
| `useVehicles.ts` | ✅ 80% | Manque recherche avec filtres |
| `useTax.ts` | ✅ 70% | Manque cache, historique |

### Traductions i18n

| Langue | État |
|--------|------|
| Français (fr.json) | ✅ Complet |
| Néerlandais (nl.json) | ✅ Complet |
| Anglais (en.json) | ✅ Complet |
