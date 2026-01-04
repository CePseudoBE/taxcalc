# VERIFICATION TAXES VEHICULES BELGIQUE

## INSTRUCTIONS IA

Tu dois verifier chaque donnee ci-dessous en visitant les URLs fournies.
Pour chaque verification, reponds par CONFIRME ou ERREUR avec la valeur correcte.

---

## VERIFICATION 1: SPECIFICATIONS VEHICULES

### GOLF_GTI

```yaml
vehicule: Volkswagen Golf GTI 2024
source_url: https://www.auto-data.net/en/volkswagen-golf-viii-facelift-2024-gti-2.0-tsi-265hp-dsg-51761
verifier:
  - puissance_ch: 265
  - puissance_kw: 195
  - co2_wltp_g: 162
  - mma_kg: 1950
```

Question: Sur la page auto-data.net, confirme que la Golf GTI 2024 a bien 265ch (195kW), 162 g/km CO2, et 1950kg MMA?

### BMW_320D

```yaml
vehicule: BMW 320d G21 Touring 2024
source_url: https://www.bmw.fr/fr/tous-les-modeles/3-series/bmw-serie-3-touring/bmw-serie-3-touring-caracteristiques-techniques.html
verifier:
  - puissance_ch: 190
  - puissance_kw: 140
  - co2_wltp_g: 148  # Source BMW officiel: 141-148 g/km
  - mma_kg: 2105  # PTAC varie selon version: 2100-2320 kg
```

Question: Sur le site BMW officiel, confirme que la BMW 320d G21 a bien 190ch (140kW), 141-148 g/km CO2?

### TESLA_MODEL_Y_PERFORMANCE

```yaml
vehicule: Tesla Model Y Performance 2024
source_url: https://www.guideautoweb.com/en/makes/tesla/model-y/2024/specifications/performance/
verifier:
  - puissance_ch: 514
  - puissance_kw: 378
  - co2_wltp_g: 0
```

Question: Sur guideautoweb.com, confirme que la Tesla Model Y Performance a bien 514ch (378kW)?

### DACIA_SANDERO_LPG

```yaml
vehicule: Dacia Sandero Stepway ECO-G 100 2024
source_url: https://www.auto-data.net/en/dacia-sandero-iii-stepway-facelift-2022-1.0-eco-g-100-91-101hp-lpg-46774
verifier:
  - puissance_ch: 101
  - puissance_kw: 74
  - co2_wltp_g: 114
  - mma_kg: 1618
```

Question: Sur auto-data.net, confirme que la Dacia Sandero ECO-G a bien 101ch (74kW), 114 g/km CO2, et 1618kg MMA?

### CLIO_TCE

```yaml
vehicule: Renault Clio TCe 100 2024
source_url: https://www.auto-data.net/en/renault-clio-v-2024-1.0-tce-100hp-52816
verifier:
  - puissance_ch: 100
  - puissance_kw: 74
  - co2_wltp_g: 128
```

Question: Sur auto-data.net, confirme que la Renault Clio TCe 100 a bien 100ch (74kW) et environ 128 g/km CO2?

---

## VERIFICATION 2: FORMULE WALLONIE TMC 2025

```yaml
source_url: https://finances.wallonie.be/home/fiscalite/fiscalite-des-vehicules/tableaux-des-baremes/taxe-de-mise-en-circulation.html
formule: TMC = MB × (CO2/X) × (MMA/Y) × C × coef_age
variables:
  X_co2_reference: 136
  Y_mma_reference: 1838
  minimum: 50
  maximum: 9000
```

Question: Sur finances.wallonie.be, confirme que:
1. La formule TMC 2025 utilise bien CO2/136 et MMA/1838?
2. Le minimum est 50 EUR et maximum 9000 EUR?

### Baremes puissance kW Wallonie

```yaml
source_url: https://finances.wallonie.be
baremes_kw:
  - tranche: "0-70"
    montant: 61.50
  - tranche: "71-85"
    montant: 123.00
  - tranche: "86-100"
    montant: 495.00
  - tranche: "101-110"
    montant: 867.00
  - tranche: "111-120"
    montant: 1239.00
  - tranche: "121-155"
    montant: 2478.00
  - tranche: "156+"
    montant: 4957.00
```

Question: Sur finances.wallonie.be, confirme ces montants de base par tranche de puissance kW?

### Coefficients energie Wallonie (vehicules electriques)

```yaml
source_url: https://finances.wallonie.be
coefficients_electrique:
  - tranche_kw: "0-70"
    coefficient: 0.01
  - tranche_kw: "71-85"
    coefficient: 0.02
  - tranche_kw: "86-100"
    coefficient: 0.04
  - tranche_kw: "101-120"
    coefficient: 0.08
  - tranche_kw: "121-155"
    coefficient: 0.12
  - tranche_kw: "156-249"
    coefficient: 0.18
  - tranche_kw: "250+"
    coefficient: 0.26
coefficient_hybride: 0.8
coefficient_thermique: 1.0
```

Question: Sur finances.wallonie.be, confirme que:
1. Les vehicules electriques ont des coefficients de 0.01 a 0.26 selon la puissance?
2. Les hybrides ont un coefficient de 0.8?
3. Les vehicules thermiques ont un coefficient de 1.0?

---

## VERIFICATION 3: FORMULE BRUXELLES TMC

```yaml
source_url: https://fiscalite.brussels/fr/taxe-de-mise-en-circulation
formule: TMC = MAX(montant_CV, montant_kW) × coef_age - reduction_LPG
variables:
  reduction_lpg: 298
  minimum_general: 61.50
  minimum_electrique: 74.29  # CORRIGE: electriques pas exoneres!
exemptions:
  - electrique: false  # CORRIGE: minimum 74.29 EUR, pas 0 EUR
  - hydrogene: false  # CORRIGE: minimum 74.29 EUR, pas 0 EUR
  - cng: "montant 0 mais pas exonere"
note: "ATTENTION: Les vehicules electriques/hydrogene NE SONT PAS exoneres a Bruxelles, ils paient le minimum de 74.29 EUR"
```

Question: Sur fiscalite.brussels, confirme que:
1. La formule prend le MAX entre montant CV et montant kW?
2. La reduction LPG est de 298 EUR?
3. Les vehicules electriques et hydrogene paient un MINIMUM de 74.29 EUR (pas exoneres)?

### Baremes Bruxelles

```yaml
source_url: https://fiscalite.brussels
baremes_cv:
  - tranche: "0-8"
    montant: 61.50
  - tranche: "9-10"
    montant: 123.00
  - tranche: "11-12"
    montant: 495.00
  - tranche: "13-14"
    montant: 867.00
  - tranche: "15-16"
    montant: 1239.00
  - tranche: "17-20"
    montant: 2478.00
  - tranche: "21+"
    montant: 4957.00
baremes_kw:
  - tranche: "0-70"
    montant: 61.50
  - tranche: "71-85"
    montant: 123.00
  - tranche: "86-100"
    montant: 495.00
  - tranche: "101-110"
    montant: 867.00
  - tranche: "111-120"
    montant: 1239.00
  - tranche: "121-155"
    montant: 2478.00
  - tranche: "156+"
    montant: 4957.00
```

Question: Sur fiscalite.brussels, confirme ces baremes par CV fiscaux et par kW?

---

## VERIFICATION 4: FORMULE FLANDRE BIV

```yaml
source_url: https://belastingen.fenb.be/ui/public/vkb/simulatie
alternative_url: https://www.vlaanderen.be/belasting-op-de-inverkeerstelling-biv
formule: BIV = bracket × euroFactor × co2Correction × coef_age
variables:
  co2_reference: 149
  co2_correction_factor: 0.003
  co2_correction_minimum: 0.5
  minimum: 61.50
exemptions:
  - electrique: true
  - hydrogene: true
  - date_limite_exemption: "31/12/2025"  # AJOUTE: verifier cette date
note: "L'exemption electrique/hydrogene en Flandre a une date limite (verifier si c'est le 31/12/2025)"
```

Question: Sur vlaanderen.be ou le simulateur FENB, confirme que:
1. La reference CO2 est 149 g/km?
2. La correction CO2 est de 0.3% par gramme d'ecart?
3. Les vehicules electriques et hydrogene sont exoneres?
4. Y a-t-il une date limite pour l'exemption electrique (ex: 31/12/2025)?

### Facteurs Euro Flandre

```yaml
source_url: https://www.vlaanderen.be/belasting-op-de-inverkeerstelling-biv
euro_factors:
  - norme: "euro_0"
    facteur: 2.0
  - norme: "euro_1"
    facteur: 1.8
  - norme: "euro_2"
    facteur: 1.5
  - norme: "euro_3"
    facteur: 1.25
  - norme: "euro_4"
    facteur: 1.0
  - norme: "euro_5"
    facteur: 0.9
  - norme: "euro_6"
    facteur: 0.8
  - norme: "euro_6d"
    facteur: 0.8
  - norme: "euro_7"
    facteur: 0.8
```

Question: Sur vlaanderen.be, confirme ces facteurs Euro pour la BIV?

---

## VERIFICATION 5: COEFFICIENTS D'AGE

```yaml
source_url: https://finances.wallonie.be/home/fiscalite/fiscalite-des-vehicules/tableaux-des-baremes/coefficient-dage.html
coefficients:
  - age: 0
    coef: 1.00
  - age: 1
    coef: 0.90
  - age: 2
    coef: 0.80
  - age: 3
    coef: 0.70
  - age: 4
    coef: 0.60
  - age: 5
    coef: 0.55
  - age: 6
    coef: 0.50
  - age: 7
    coef: 0.45
  - age: 8
    coef: 0.40
  - age: 9
    coef: 0.35
  - age: 10
    coef: 0.30
  - age: 11
    coef: 0.25
  - age: 12
    coef: 0.20
  - age: 13
    coef: 0.15
  - age: 14
    coef: 0.10
  - age: 15
    coef: 0.00
note: "15 ans et plus = exonere (coefficient 0)"
```

Question: Sur finances.wallonie.be, confirme ces coefficients d'age?

---

## VERIFICATION 6: TAXE ANNUELLE

```yaml
source_url: https://finances.wallonie.be/home/fiscalite/fiscalite-des-vehicules/tableaux-des-baremes/taxe-de-circulation.html
baremes_cv:
  - cv: 4
    montant: 98.02
  - cv: 5
    montant: 130.46
  - cv: 6
    montant: 179.30
  - cv: 7
    montant: 228.14
  - cv: 8
    montant: 276.98
  - cv: 9
    montant: 374.90
  - cv: 10
    montant: 472.58
  - cv: 15
    montant: 962.18
  - cv: 20
    montant: 1941.38
supplement_lpg_par_cv: 99.16
```

Question: Sur finances.wallonie.be, confirme ces montants de taxe annuelle par CV fiscal?

### Flandre taxe annuelle CO2

```yaml
source_url: https://www.vlaanderen.be/verkeersbelasting-op-personenwagens
formule: taxe = base_cv × (1 + (CO2 - 149) × 0.003)
exemptions:
  - electrique: true
  - hydrogene: true
```

Question: Sur vlaanderen.be, confirme que:
1. La taxe annuelle en Flandre a un ajustement CO2 de 0.3% par gramme vs 149?
2. Les vehicules electriques sont exoneres de taxe annuelle en Flandre?

---

## VERIFICATION 7: CALCULS A VALIDER AVEC SIMULATEURS

### Test 1: Golf GTI Wallonie

```yaml
simulateur: https://www.moniteurautomobile.be/actu-auto/fiscalite/simulateur-de-taxes-auto.html
entrer:
  vehicule: Golf GTI 2024
  puissance_kw: 195
  co2_g: 162
  mma_kg: 1950
  region: Wallonie
  age: 0
resultat_attendu:
  tmc: 6263
  tolerance: 200
```

Question: Sur le simulateur Moniteur Automobile, entre une Golf GTI (195kW, 162g CO2, 1950kg) en Wallonie. Le resultat est-il proche de 6263 EUR?

### Test 2: Golf GTI Bruxelles

```yaml
simulateur: https://fiscalite.brussels/fr/simulateur
entrer:
  puissance_kw: 195
  cv_fiscaux: 15
  region: Bruxelles
  age: 0
resultat_attendu:
  tmc: 4957
```

Question: Sur le simulateur Bruxelles, entre 195kW et 15CV. Le resultat est-il 4957 EUR?

### Test 3: Golf GTI Flandre

```yaml
simulateur: https://belastingen.fenb.be/ui/public/vkb/simulatie
entrer:
  puissance_kw: 195
  co2_g: 162
  euro_norm: Euro6d
  age: 0
resultat_attendu:
  biv: 4120
  tolerance: 100
```

Question: Sur le simulateur FENB Flandre, entre 195kW, 162g CO2, Euro 6d. Le resultat est-il proche de 4120 EUR?

### Test 4: Tesla Model Y Bruxelles

```yaml
simulateur: https://fiscalite.brussels/fr/simulateur
entrer:
  type: electrique
  region: Bruxelles
resultat_attendu:
  tmc: 74.29  # CORRIGE: pas 0 EUR
  exonere: false  # CORRIGE
note: "Bruxelles electrique = minimum 74.29 EUR, pas exemption totale"
```

Question: Sur le simulateur Bruxelles, un vehicule electrique paie-t-il le minimum de 74.29 EUR (pas 0)?

### Test 5: Tesla Model Y Wallonie

```yaml
simulateur: https://www.moniteurautomobile.be/actu-auto/fiscalite/simulateur-de-taxes-auto.html
entrer:
  vehicule: Tesla Model Y Performance
  puissance_kw: 378
  type: electrique
  region: Wallonie
resultat_attendu:
  tmc: 1766
  tolerance: 200
  note: "Wallonie ne donne PAS d'exemption aux electriques"
```

Question: Sur le simulateur, une Tesla Model Y Performance (378kW, electrique) en Wallonie donne-t-elle environ 1766 EUR (pas 0)?

---

## VERIFICATION 8: DIFFERENCES REGIONALES CRITIQUES

### Electriques TMC

```yaml
question: "Les vehicules electriques sont-ils exoneres de TMC?"
reponses_attendues:
  wallonie: "NON - coefficient reduit (0.01-0.26) mais pas exonere"
  bruxelles: "NON - minimum 74.29 EUR (pas 0 EUR!)"  # CORRIGE
  flandre: "OUI - 100% exonere (jusqu'au 31/12/2025)"
sources:
  - https://finances.wallonie.be
  - https://fiscalite.brussels
  - https://www.vlaanderen.be/belasting-op-de-inverkeerstelling-biv
```

Question: Confirme que:
1. La Wallonie applique un coefficient reduit (0.01-0.26) pour les electriques
2. Bruxelles fait payer un MINIMUM de 74.29 EUR aux electriques (pas exoneres!)
3. La Flandre exonere completement les electriques (verifier date limite)

### Electriques taxe annuelle

```yaml
question: "Les vehicules electriques sont-ils exoneres de taxe annuelle?"
reponses_attendues:
  wallonie: "NON - taxe normale selon CV"
  bruxelles: "NON - taxe normale selon CV"
  flandre: "OUI - 100% exonere"
sources:
  - https://finances.wallonie.be
  - https://fiscalite.brussels
  - https://www.vlaanderen.be/verkeersbelasting-op-personenwagens
```

Question: Confirme que seule la Flandre exonere les electriques de taxe annuelle?

### LPG

```yaml
question: "Comment le LPG est-il traite?"
reponses_attendues:
  wallonie_tmc: "Pas de reduction"
  bruxelles_tmc: "Reduction de 298 EUR"
  flandre_tmc: "Pas de reduction"
  annuel_partout: "Supplement de 99.16 EUR par CV fiscal"
```

Question: Confirme que seule Bruxelles donne une reduction TMC de 298 EUR pour le LPG, et que toutes les regions appliquent un supplement annuel de 99.16 EUR/CV?

---

## RESUME DES CALCULS A VERIFIER

| Vehicule | Wallonie TMC | Bruxelles TMC | Flandre BIV | Source verification |
|----------|--------------|---------------|-------------|---------------------|
| Golf GTI neuf | 6263 EUR | 4957 EUR | 4120 EUR | Simulateurs |
| BMW 320d neuf (CO2=148g) | 3087 EUR | 2478 EUR | 1976 EUR | Simulateurs |
| Tesla Y Perf neuf | 1766 EUR | 74.29 EUR (min) | 0 EUR (exonere) | Simulateurs |
| Dacia Sandero LPG | 91 EUR | 62 EUR | 88 EUR | Simulateurs |
| Clio TCe neuf | 101 EUR | 123 EUR | 92 EUR | Simulateurs |

Notes:
- BMW 320d: CO2=148 g/km (source BMW officiel), MMA=2105 kg
- Tesla Bruxelles: pas exonere, paie minimum 74.29 EUR (indexe juillet 2024)

---

## FORMAT DE REPONSE ATTENDU

Pour chaque verification, reponds dans ce format:

```
VERIFICATION X: [TITRE]
STATUS: CONFIRME / ERREUR / PARTIEL
DETAILS: [explication]
VALEUR_CORRECTE: [si erreur, quelle est la bonne valeur]
SOURCE: [URL visitee]
```

---

Document genere: 2025-12-30
Version: 4.0 - Format optimise verification IA
