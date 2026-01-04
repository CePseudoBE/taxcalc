# Belgian Vehicle Tax Formulas & Brackets

Document for AI verification of tax calculation methods.
Last updated: January 2026

---

## 1. FLANDERS - BIV (Belasting op Inverkeerstelling)

### Official Formulas (DUAL FORMULA based on registration date)

**Pre-2021 vehicles (NEDC):**
```
BIV = (((CO2 × f + x) / 246)^6 × 4500 + c) × LC
```

**2021+ vehicles (WLTP):**
```
BIV = (((CO2 × f × q) / 246)^6 × 4500 + c) × LC
```

Where:
- **CO2** = CO2 emissions (g/km) - NEDC for pre-2021, WLTP for 2021+
- **f** = Fuel factor
- **x** = CO2 correction term (additive, for NEDC vehicles only)
- **q** = WLTP correction factor (multiplicative, for 2021+ vehicles)
- **c** = Luchtcomponent (air component, depends on Euro norm and fuel type)
- **LC** = Age coefficient (leeftijdscoefficient)

### Fuel Factor (f)

| Fuel Type | Factor |
|-----------|--------|
| LPG | 0.88 |
| CNG | 0.93 |
| Bicarburation CNG (petrol+CNG homologated as petrol) | 0.744 |
| All others (petrol, diesel, hybrid) | 1.00 |

### CO2 Correction Term (x) - NEDC vehicles only

| Year | Value (g/km) |
|------|--------------|
| 2020 | 36.0 |
| 2021 | 40.5 |
| 2022 | 45.0 |
| 2023 | 49.5 |
| 2024 | 54.0 |
| 2025 | 58.5 |
| 2026 | 63.0 |

Formula: `x = 36 + (year - 2020) × 4.5`

### WLTP Q Factor (q) - 2021+ vehicles only

| Year | Value |
|------|-------|
| 2021 | 1.070 |
| 2022 | 1.105 |
| 2023 | 1.140 |
| 2024 | 1.175 |
| 2025 | 1.210 |
| 2026 | 1.245 |

Formula: `q = 1.07 + (year - 2021) × 0.035`

### Luchtcomponent (c) - July 2025 (indexed +2%)

| Euro Norm | Diesel (EUR) | Petrol/LPG/CNG (EUR) |
|-----------|--------------|----------------------|
| Euro 0    | 3,809.68     | 1,515.25             |
| Euro 1    | 1,081.36     | 655.86               |
| Euro 2    | 2,385.26     | 907.59               |
| Euro 3    | 1,493.77     | 567.39               |
| Euro 4    | 907.59       | 340.55               |
| Euro 5    | 567.39       | 113.33               |
| Euro 6/7  | 604.18       | 27.43                |

**Note:** Euro 0 and Euro 1 have DIFFERENT values (previously incorrectly shown as identical).

### Formula Constants

| Parameter | Value |
|-----------|-------|
| Divisor | 246 |
| Multiplier | 4500 |
| Min amount (2025) | 55.88 EUR |
| Max amount (2025) | 13,969.29 EUR |

### Electric/Hydrogen Vehicles

| Period | Treatment |
|--------|-----------|
| Before 2026 | Fully exempt |
| From 2026 | Fixed 61.50 EUR |

### Example: Euro 6 Petrol, 162 g/km CO2 (Golf GTI, new car, 2026)

```
f = 1.0 (petrol)
x = 63 (2026)
c = 27.43 EUR (petrol Euro 6)
LC = 1.0 (new)

Step 1: CO2 × f + x = 162 × 1.0 + 63 = 225
Step 2: / 246 = 0.9146
Step 3: ^6 = 0.5858
Step 4: × 4500 = 2636.10
Step 5: + c = 2636.10 + 27.43 = 2663.53 EUR

BIV = 2,663.53 EUR

With age coefficients:
- New (0y): 2663.53 × 1.00 = 2,663.53 EUR
- 5 years: 2663.53 × 0.55 = 1,464.94 EUR
- 10 years: 2663.53 × 0.30 = 799.06 EUR
```

---

## 2. BRUSSELS - TMC

### Formula

```
TMC = MAX(Amount_CV, Amount_kW) × AgeCoef - LPG_Reduction
```

Where:
- Compare AMOUNTS from both brackets (fiscal HP and power kW)
- Take the HIGHER amount
- Apply age coefficient
- Subtract LPG reduction if applicable

### Special Cases

- **Electric/Hydrogen**: Minimum amount = 74.29 EUR (not exempt)
- **CNG**: Fully exempt (0 EUR)
- **LPG**: -360 EUR reduction (indexed from 298 EUR)

### Power Brackets (kW) & Fiscal HP Brackets - July 2024 (+20.79%)

| Power (kW) | Fiscal HP | Amount (EUR) |
|------------|-----------|--------------|
| 0-70       | 0-8       | 74.29        |
| 71-85      | 9-10      | 148.57       |
| 86-100     | 11-12     | 597.91       |
| 101-110    | 13-14     | 1,047.05     |
| 111-120    | 15-16     | 1,496.18     |
| 121-155    | 17-20     | 2,993.39     |
| 156+       | 21+       | 5,988.51     |

### Example: Golf GTI (195kW, 15CV)

```
Base amount (indexed +20.79%): 5,988.51 EUR

With age coefficients:
- New (0y): 5988.51 × 1.00 = 5,988.51 EUR
- 5 years: 5988.51 × 0.55 = 3,293.68 EUR
- 10 years: 5988.51 × 0.30 = 1,796.55 EUR
```

---

## 3. WALLONIA - TMC (Reform July 2025)

### Formula

```
TMC = MB × (CO2 / X) × (MMA / Y) × C × AgeCoef
```

Where:
- **MB** = Base amount (from power_kw bracket)
- **CO2** = Vehicle CO2 emissions (g/km)
- **X** = CO2 reference: 136 (WLTP) or 115 (NEDC)
- **MMA** = Maximum Authorized Mass (kg)
- **Y** = MMA reference: 1838 kg
- **C** = Energy coefficient (fuel type)
- **AgeCoef** = Age coefficient (0-15 years)

### Power Brackets (MB) - Valid from 2025-07-01

| Power (kW) | Amount (EUR) |
|------------|--------------|
| 0-70       | 61.50        |
| 71-85      | 123.00       |
| 86-100     | 495.00       |
| 101-110    | 867.00       |
| 111-120    | 1,239.00     |
| 121-155    | 2,478.00     |
| 156+       | 4,957.00     |

### Energy Coefficients (C)

| Fuel Type | Coefficient |
|-----------|-------------|
| Thermal (petrol, diesel, LPG, CNG) | 1.0 |
| Hybrid (all types) | 0.8 |
| Electric/Hydrogen (0-120 kW) | 0.01 |
| Electric/Hydrogen (121-155 kW) | 0.10 |
| Electric/Hydrogen (156-249 kW) | 0.18 |
| Electric/Hydrogen (250+ kW) | 0.26 |

### Parameters

| Parameter | Value |
|-----------|-------|
| co2_reference_wltp | 136 g/km |
| co2_reference_nedc | 115 g/km |
| mma_reference | 1838 kg |
| min_amount | 50.00 EUR |
| max_amount | 9,000.00 EUR |

### Example: Diesel 110kW, 150g CO2, 1800kg

```
MB = 867.00 EUR (101-110 kW bracket)
CO2 factor = 150 / 136 = 1.1029
MMA factor = 1800 / 1838 = 0.9793
Energy coef = 1.0 (thermal)
Age coef = 1.0 (new)

TMC = 867.00 × 1.1029 × 0.9793 × 1.0 × 1.0
TMC = 936.25 EUR
```

---

## 4. AGE COEFFICIENTS (All Regions)

| Age (years) | Brussels/Wallonia | Flanders |
|-------------|-------------------|----------|
| 0 (new)     | 1.00              | 1.00     |
| 1           | 0.90              | 0.90     |
| 2           | 0.80              | 0.80     |
| 3           | 0.70              | 0.70     |
| 4           | 0.60              | 0.60     |
| 5           | 0.55              | 0.55     |
| 6           | 0.50              | 0.50     |
| 7           | 0.45              | 0.45     |
| 8           | 0.40              | 0.40     |
| 9           | 0.35              | 0.35     |
| 10          | 0.30              | 0.30     |
| 11          | 0.25              | 0.25     |
| 12          | 0.20              | 0.20     |
| 13          | 0.15              | 0.15     |
| 14          | 0.10              | 0.10     |
| 15+         | 0.00 (min 61.50€) | 0.10 (min BIV) |

**Note:** In Flanders, vehicles 15+ years old are NOT exempt but pay 10% of the BIV (minimum ~55-57€).
In Brussels/Wallonia, 15+ year vehicles pay the minimum amount (61.50€ for admin purposes).

---

## 5. ANNUAL TAX (All Regions)

### Base Formula

```
Annual = BaseAmount + LPG_Supplement
```

Flanders adds CO2 adjustment for non-electric vehicles.

### Fiscal HP Brackets (All Regions) - 2025 (indexed +2%)

| Fiscal HP | Amount (EUR) |
|-----------|--------------|
| 0-4       | 100.00       |
| 5         | 133.07       |
| 6         | 182.89       |
| 7         | 232.70       |
| 8         | 282.52       |
| 9         | 382.40       |
| 10        | 482.03       |
| 11        | 581.91       |
| 12        | 681.79       |
| 13        | 781.67       |
| 14        | 881.55       |
| 15        | 981.42       |
| 16        | 1,181.18     |
| 17        | 1,380.94     |
| 18        | 1,580.69     |
| 19        | 1,780.45     |
| 20+       | 1,980.21     |

### LPG Supplement

```
LPG_Supplement = fiscal_hp × 101.14 EUR
```

### Electric/Hydrogen (Annual Tax)

| Region | Status |
|--------|--------|
| Wallonia | Not exempt (pay based on fiscal HP) |
| Brussels | Not exempt (pay based on fiscal HP) |
| Flanders (before 2026) | Exempt |
| Flanders (from 2026) | Min 69.72 - 87.24 EUR |

---

## 6. CODE IMPLEMENTATION

### Main Service
`TaxCalculationService.java`

### Key Methods

| Method | Description |
|--------|-------------|
| `calculateFlandersTmc()` | Official BIV formula: (((CO2×f+x)/246)^6×4500+c)×LC |
| `getFlandersFuelFactor()` | Returns f factor by fuel type |
| `getFlandersLuchtcomponent()` | Returns c by Euro norm and fuel |
| `calculateWalloniaTmc()` | Wallonia formula: MB × CO2 × MMA × C × Age |
| `calculateBrusselsTmc()` | Brussels formula: MAX(CV, kW) × Age - LPG |

### Database Tables

| Table | Purpose |
|-------|---------|
| `tax_parameters` | Formula constants (x, f, divisor, etc.) |
| `tax_brackets` | Amount brackets (power_kw, luchtcomponent, etc.) |
| `age_coefficients` | Age-based reduction coefficients |
| `tax_exemptions` | Exemption conditions by fuel type |

---

## 7. DATA SOURCES

- Flanders: [Vlaamse Codex Fiscaliteit](https://codex.vlaanderen.be)
- Flanders: [Kluwer BIV Tools](https://tools.kluwer.be/BIV_AB/information_nl/BIV.htm)
- Wallonia: [SPW Finances](https://finances.wallonie.be)
- Brussels: [Fiscalite Brussels](https://fiscalite.brussels)
- Indexation: [Fleet.be](https://www.fleet.be), [NSZ.be](https://www.nsz.be)
