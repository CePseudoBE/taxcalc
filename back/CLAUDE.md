# Back-Taxes - Belgian Vehicle Tax Calculator

## Project Overview

Backend API for calculating Belgian vehicle taxes (TMC and annual circulation tax) for all three regions: Wallonia, Brussels, and Flanders.

## Tech Stack

- **Language**: Java 24
- **Framework**: Spring Boot 4.0.0
- **Database**: PostgreSQL (prod) / H2 (dev)
- **ORM**: Spring Data JPA
- **Migrations**: Liquibase (YAML format)
- **Build**: Gradle
- **Tests**: JUnit 5, TestContainers

## Project Structure

```
src/main/java/be/hoffmann/backtaxes/
├── controller/     # REST API endpoints
├── service/        # Business logic (tax calculations)
├── repository/     # JPA repositories
├── entity/         # JPA entities + enums
├── dto/            # Request/Response DTOs
├── config/         # Security & CORS configuration
└── exception/      # Exception handling

src/main/resources/
├── db/changelog/   # Liquibase migrations
│   └── changes/    # Individual changesets (001-019)
├── application.properties      # Production config
└── application-dev.properties  # Development config
```

## Key Files

| File | Purpose |
|------|---------|
| `TaxCalculationService.java` | Core tax calculation engine with regional formulas |
| `TaxConfigService.java` | Tax parameters, brackets, and exemptions management |
| `TaxController.java` | REST endpoints for tax calculations |
| `007-seed-wallonia-tmc-2025.yaml` | Wallonia TMC brackets and parameters |
| `008-seed-brussels-tmc.yaml` | Brussels TMC brackets |
| `009-seed-flanders-tmc.yaml` | Flanders BIV brackets |
| `010-seed-annual-taxes.yaml` | Annual tax brackets (all regions) |
| `TAX_FORMULAS.md` | Complete tax formulas and brackets documentation |

## Running the Project

```bash
# Development mode (H2 in-memory)
./gradlew bootRun --args='--spring.profiles.active=dev'

# Production mode (requires PostgreSQL)
./gradlew bootRun

# Run tests
./gradlew test
```

## API Endpoints

### Tax Calculation
- `POST /api/tax/calculate` - Calculate both TMC and annual tax
- `POST /api/tax/tmc` - Calculate TMC only
- `POST /api/tax/annual` - Calculate annual tax only

### Vehicle Catalog
- `GET /api/brands` - List all brands
- `GET /api/brands/{id}/models` - Models for a brand
- `GET /api/models/{id}/variants` - Variants for a model
- `GET /api/variants/{id}` - Variant details

## Tax Calculation Logic

See `TAX_FORMULAS.md` for complete documentation of:
- Regional formulas (Wallonia, Brussels, Flanders)
- Tax brackets and rates
- Age coefficients
- Exemptions and reductions
- Parameters (CO2 reference, MMA reference, etc.)

## Conventions

- **Enums**: Stored as PostgreSQL native enums (region, tax_type, fuel_type, euro_norm)
- **Dates**: Use `valid_from`/`valid_to` for versioning tax parameters
- **Money**: Use `BigDecimal` with 2 decimal places, `HALF_UP` rounding
- **Tests**: Integration tests in `src/test/java/.../integration/`

## Common Tasks

### Add new tax bracket
1. Create new Liquibase changeset in `src/main/resources/db/changelog/changes/`
2. Insert into `tax_brackets` table with appropriate `valid_from` date

### Update tax parameters
1. Either update existing parameter's `valid_to` and insert new one
2. Or create UPDATE migration for bulk indexation

### Add new vehicle variant
1. Use `POST /api/submissions` endpoint
2. Moderator approves via `POST /api/submissions/{id}/review`

## Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/backtaxes` | Database URL |
| `SPRING_DATASOURCE_USERNAME` | `postgres` | DB username |
| `SPRING_DATASOURCE_PASSWORD` | `password` | DB password |
