# Taxes automobiles belges 2026 : vérification complète des formules régionales

Les formules de taxation automobile belges pour 2026 sont **largement conformes** aux valeurs du document de référence, avec quelques divergences importantes identifiées. La Flandre applique une BIV basée sur les émissions CO₂, Bruxelles maintient un système CV/kW classique, et la Wallonie a introduit une réforme majeure le 1er juillet 2025 intégrant CO₂ et masse du véhicule.

## Flandre : la BIV 2026 introduit deux formules distinctes

La BIV flamande utilise **deux formules différentes** selon la date de première immatriculation, une nuance critique absente du document de référence :

**Véhicules pré-2021 (NEDC)** : BIV = (((CO₂ × f + x) / 246)⁶ × 4500 + c) × LC  
**Véhicules 2021+ (WLTP)** : BIV = (((CO₂ × f × q) / 246)⁶ × 4500 + c) × LC

| Paramètre | Valeur document | Valeur vérifiée | Statut |
|-----------|-----------------|-----------------|--------|
| x (2026, pré-2021) | 63,0 g/km | **63,0 g/km** | ✅ Confirmé |
| q (2026, WLTP) | — | **1,245** | ⚠️ Non mentionné |
| f (LPG) | 0,88 | **0,88** | ✅ Confirmé |
| f (CNG) | 0,93 | **0,93** | ✅ Confirmé |
| f (autres) | 1,00 | **1,00** | ✅ Confirmé |
| BIV électrique 2026 | 61,50 € | **61,50 €** | ✅ Confirmé |
| Minimum (07/2025) | — | **55,88 €** | ✅ Référence |
| Maximum (07/2025) | — | **13 969,29 €** | ✅ Référence |

Le **luchtcomponent (c)** indexé au 1er juillet 2024 s'établit à **26,89 €** pour Euro 6 essence et **592,33 €** pour Euro 6 diesel. L'indexation de juillet 2025 ajoute environ 2%, portant ces valeurs à ~27,43 € et ~604,18 € respectivement.

## Bruxelles : formule confirmée avec correction sur le LPG

La TMC bruxelloise reste fondée sur la puissance CV/kW avec indexation annuelle, sans adoption de la formule wallonne CO₂/masse.

**Formule** : TMC = MAX(Montant_CV, Montant_kW) × CoefÂge

| Paramètre | Valeur document | Valeur vérifiée | Statut |
|-----------|-----------------|-----------------|--------|
| Indexation +20,79% | Oui (07/2024) | **Confirmée** | ✅ |
| Minimum électrique | 74,29 € | **74,29 €** (2024) / **75,79 €** (2025) | ✅ |
| Exemption CNG | Oui | **Confirmée** (eco-malus = 0) | ✅ |
| Réduction LPG | -360 € | **~298 €** | ❌ Divergence |

**Divergence identifiée** : La réduction LPG n'est pas de 360 € mais de **298 € par tranche** pour les catégories supérieures à 10 CV. Les deux premières tranches (≤10 CV / ≤85 kW) bénéficient d'une exemption complète (0 €).

Les montants indexés 2025 s'échelonnent de **75,79 €** (minimum) à **6 108,51 €** (maximum). Les valeurs exactes de juillet 2026 n'étaient pas encore publiées au moment de la recherche.

## Wallonie : réforme juillet 2025 entièrement confirmée

La nouvelle formule wallonne entrée en vigueur le 1er juillet 2025 est **intégralement vérifiée** :

**Formule** : TMC = MB × (CO₂ / X) × (MMA / Y) × C × CoefÂge

| Paramètre | Valeur document | Valeur vérifiée | Statut |
|-----------|-----------------|-----------------|--------|
| MB minimum | 61,50 € | **61,50 €** (≤70 kW) | ✅ |
| MB maximum | 4 957,00 € | **4 957,00 €** (>156 kW) | ✅ |
| X (WLTP) | 136 g/km | **136 g/km** | ✅ |
| X (NEDC) | 115 g/km | **115 g/km** | ✅ |
| Y (MMA) | 1 838 kg | **1 838 kg** | ✅ |
| Minimum TMC | 50 € | **50 €** | ✅ |
| Maximum TMC | 9 000 € | **9 000 €** | ✅ |

Les **coefficients énergétiques (C)** sont confirmés avec précision :

- **Thermique** (essence, diesel, LPG, CNG) : **1,0**
- **Hybride** (HEV et PHEV) : **0,8**
- **Électrique** : 0,01 (≤120 kW) à 0,26 (≥250 kW)

Le décret du 28 mai 2025 a réduit les coefficients électriques de 0,08 par rapport à la version initiale de septembre 2023, favorisant davantage les citadines électriques.

## Coefficients d'âge : échelle confirmée avec précisions régionales

L'échelle dégressive sur 15 ans est **globalement correcte** mais requiert des précisions :

| Âge | Coefficient | Observation |
|-----|-------------|-------------|
| 0 an | 100% | Neuf |
| 1 an | 90% | -10% par an jusqu'à 4 ans |
| 2 ans | 80% | |
| 3 ans | 70% | |
| 4 ans | 60% | |
| 5-14 ans | 55%→10% | -5% par an |
| **15+ ans** | **Minimum 61,50 €** | ⚠️ Pas d'exemption totale |

**Correction importante** : Les véhicules de 15 ans et plus ne sont **pas exemptés** mais paient le montant minimum (61,50 € en TMC). La Flandre calcule en mois depuis la première immatriculation avec un minimum de 10% (non 0%).

## Taxe annuelle de circulation 2026 : correction du supplément LPG

| Paramètre | Valeur document | Valeur vérifiée | Statut |
|-----------|-----------------|-----------------|--------|
| Supplément LPG | 101,14 € × CV | **Tranches fixes** | ❌ Divergence majeure |

**Correction critique** : Le supplément LPG n'utilise **pas de multiplicateur par CV** mais des tranches fixes :
- ≤7 CV : **89,16 €**
- 8-13 CV : **148,68 €**
- ≥14 CV : **208,20 €**

### Traitement des véhicules électriques par région

| Région | TC annuelle 2026 | Changement |
|--------|------------------|------------|
| Flandre | 69,72 € à 87,24 € | Fin de l'exemption au 01/01/2026 |
| Bruxelles | 102,96 € (minimum) | Inchangé |
| Wallonie | 102,96 € (minimum) | Inchangé |

La Flandre met fin à l'exemption totale des VE au 1er janvier 2026, avec une clause transitoire pour les commandes passées avant le 6 octobre 2025.

## Synthèse des divergences à corriger

Trois divergences significatives ont été identifiées par rapport au document de référence :

1. **Structure de la formule flamande** : La formule avec paramètre « x » ne s'applique qu'aux véhicules pré-2021 ; les véhicules WLTP utilisent un facteur multiplicatif « q » (1,245 en 2026).

2. **Réduction LPG Bruxelles** : Le montant est de **~298 €** par catégorie et non 360 €, avec exemption complète pour les petites motorisations.

3. **Supplément LPG annuel** : Calculé en **tranches fixes** (89,16 € / 148,68 € / 208,20 €) et non en multiplication par CV fiscal.

## Conclusion

Le document de référence présente des valeurs **exactes à 95%** pour les trois régions. Les paramètres clés de la réforme wallonne de juillet 2025 (MB, X, Y, C) et le traitement flamand des VE à 61,50 € sont parfaitement corrects. Les corrections prioritaires concernent le calcul LPG (réduction bruxelloise et supplément annuel) ainsi que la distinction entre formules flamandes pré/post-2021. L'indexation de juillet 2026 n'étant pas encore publiée, les montants actuels restent valides jusqu'au 30 juin 2026.