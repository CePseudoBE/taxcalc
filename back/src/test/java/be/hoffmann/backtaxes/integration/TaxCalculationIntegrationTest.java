package be.hoffmann.backtaxes.integration;

import be.hoffmann.backtaxes.dto.response.TaxCalculationResponse;
import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.service.TaxCalculationService;
import be.hoffmann.backtaxes.service.TaxCalculationService.VehicleData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for tax calculations using real database with seed data.
 * Uses Testcontainers to run a PostgreSQL instance with Liquibase migrations.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("integration")
class TaxCalculationIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private TaxCalculationService taxCalculationService;

    // Reference date for Wallonia TMC 2025 (reform starts July 2025)
    private static final LocalDate WALLONIA_2025_DATE = LocalDate.of(2025, 8, 1);
    // Reference date for Brussels/Flanders (valid from July 2024)
    private static final LocalDate BRUSSELS_DATE = LocalDate.of(2024, 8, 1);

    @Nested
    @DisplayName("Wallonia TMC 2025")
    class WalloniaTmcIntegrationTests {

        @Test
        @DisplayName("Electric vehicle should have very low TMC (no exemption in Wallonia)")
        void electricVehicleShouldHaveVeryLowTmc() {
            // In Wallonia, electric vehicles are NOT exempt but have a very low energy coefficient (0.01-0.26)
            VehicleData vehicle = new VehicleData(
                    100,      // powerKw (0-120 kW bracket = 0.01 coefficient)
                    8,
                    FuelType.electric,
                    EuroNorm.euro_6d,
                    0,
                    null,
                    1800
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.wallonia,
                    TaxType.tmc,
                    WALLONIA_2025_DATE
            );

            // Electric vehicles in Wallonia are NOT exempt, but should have very low amount
            assertThat(response.getIsExempt()).isFalse();
            // With coefficient 0.01, amount should be minimal (base * 0.01)
            assertThat(response.getAmount()).isLessThan(BigDecimal.valueOf(100));
        }

        @Test
        @DisplayName("Standard petrol vehicle - new car")
        void standardPetrolVehicleNewCar() {
            // 110 kW petrol, Euro 6d, 150 g/km CO2, 1800 kg
            VehicleData vehicle = new VehicleData(
                    110,
                    8,
                    FuelType.petrol,
                    EuroNorm.euro_6d,
                    150,
                    null,
                    1800
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.wallonia,
                    TaxType.tmc,
                    WALLONIA_2025_DATE
            );

            assertThat(response.getIsExempt()).isFalse();
            assertThat(response.getAmount()).isNotNull();
            assertThat(response.getAmount()).isGreaterThan(BigDecimal.ZERO);
            // Amount should be between min (50) and max (9000)
            assertThat(response.getAmount()).isGreaterThanOrEqualTo(BigDecimal.valueOf(50));
            assertThat(response.getAmount()).isLessThanOrEqualTo(BigDecimal.valueOf(9000));
        }

        @Test
        @DisplayName("Old vehicle (15 years) should have zero TMC due to age coefficient")
        void oldVehicleShouldHaveZeroTmc() {
            VehicleData vehicle = new VehicleData(
                    150,
                    10,
                    FuelType.diesel,
                    EuroNorm.euro_4,
                    180,
                    null,
                    2000
            );

            // Vehicle registered 15 years before the calculation date
            LocalDate oldRegistrationDate = WALLONIA_2025_DATE.minusYears(15);

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.wallonia,
                    TaxType.tmc,
                    oldRegistrationDate
            );

            // Age coefficient at 15 years = 0, so amount should be 0
            assertThat(response.getBreakdown()).containsKey("ageCoefficient");
            BigDecimal ageCoef = new BigDecimal(response.getBreakdown().get("ageCoefficient").toString());
            assertThat(ageCoef).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("High power vehicle should respect max amount (9000 EUR)")
        void highPowerVehicleShouldRespectMaxAmount() {
            VehicleData vehicle = new VehicleData(
                    400,
                    25,
                    FuelType.petrol,
                    EuroNorm.euro_6d,
                    300,
                    null,
                    2500
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.wallonia,
                    TaxType.tmc,
                    WALLONIA_2025_DATE
            );

            // Max amount is 9000 EUR for Wallonia TMC 2025
            assertThat(response.getAmount()).isLessThanOrEqualTo(BigDecimal.valueOf(9000));
        }
    }

    @Nested
    @DisplayName("Brussels TMC")
    class BrusselsTmcIntegrationTests {

        @Test
        @DisplayName("Electric vehicle should pay minimum 74.29 EUR (not exempt)")
        void electricVehicleShouldPayMinimum() {
            VehicleData vehicle = new VehicleData(
                    100,
                    7,
                    FuelType.electric,
                    EuroNorm.euro_6d,
                    0,
                    null,
                    1600
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.brussels,
                    TaxType.tmc,
                    BRUSSELS_DATE
            );

            // Brussels electric vehicles are NOT exempt - they pay minimum 74.29 EUR
            assertThat(response.getIsExempt()).isFalse();
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(74.29));
        }

        @Test
        @DisplayName("LPG vehicle should have reduction applied")
        void lpgVehicleShouldHaveReduction() {
            VehicleData vehicle = new VehicleData(
                    100,
                    8,
                    FuelType.lpg,
                    EuroNorm.euro_6d,
                    140,
                    null,
                    1700
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.brussels,
                    TaxType.tmc,
                    BRUSSELS_DATE
            );

            assertThat(response.getBreakdown()).containsKey("lpgReduction");
            // LPG reduction is 298 EUR, indexed +20.79% = 359.95 EUR
            Object lpgReduction = response.getBreakdown().get("lpgReduction");
            assertThat(new BigDecimal(lpgReduction.toString())).isEqualByComparingTo(BigDecimal.valueOf(359.95));
        }
    }

    @Nested
    @DisplayName("Flanders TMC (BIV)")
    class FlandersTmcIntegrationTests {

        @Test
        @DisplayName("Electric vehicle pays fixed 61.50€ from 2026")
        void electricVehiclePaysFixedAmountFrom2026() {
            VehicleData vehicle = new VehicleData(
                    150,
                    10,
                    FuelType.electric,
                    EuroNorm.euro_6d,
                    0,
                    null,
                    2000
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.flanders,
                    TaxType.tmc,
                    BRUSSELS_DATE
            );

            // Since 2026, electric vehicles pay a fixed 61.50€ in Flanders
            assertThat(response.getIsExempt()).isFalse();
            assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("61.50"));
        }
    }

    @Nested
    @DisplayName("Annual Tax - All Regions")
    class AnnualTaxIntegrationTests {

        @Test
        @DisplayName("Wallonia annual tax based on fiscal HP (8 CV = 282.52 EUR indexed)")
        void walloniaAnnualTaxBasedOnFiscalHp() {
            VehicleData vehicle = new VehicleData(
                    110,
                    8,      // 8 CV fiscal -> 282.52 EUR (indexed +2%)
                    FuelType.petrol,
                    EuroNorm.euro_6d,
                    150,
                    null,
                    1800
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.wallonia,
                    TaxType.annual,
                    BRUSSELS_DATE
            );

            assertThat(response.getIsExempt()).isFalse();
            // For 8 CV, amount is 282.52 EUR after July 2025 indexation (+2%)
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(282.52));
        }

        @Test
        @DisplayName("LPG vehicle should have supplement (101.14 EUR per HP indexed)")
        void lpgVehicleShouldHaveSupplement() {
            VehicleData vehicle = new VehicleData(
                    100,
                    8,
                    FuelType.lpg,
                    EuroNorm.euro_6d,
                    140,
                    null,
                    1700
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.wallonia,
                    TaxType.annual,
                    BRUSSELS_DATE
            );

            assertThat(response.getBreakdown()).containsKey("lpgSupplement");
            // LPG supplement is 101.14 EUR per HP (indexed +2%) = 8 * 101.14 = 809.12 EUR
            BigDecimal expectedSupplement = BigDecimal.valueOf(101.14).multiply(BigDecimal.valueOf(8));
            Object lpgSupplement = response.getBreakdown().get("lpgSupplement");
            assertThat(new BigDecimal(lpgSupplement.toString()).setScale(2))
                    .isEqualByComparingTo(expectedSupplement.setScale(2));
        }

        @Test
        @DisplayName("Electric vehicle exempt from Flanders annual tax")
        void electricVehicleExemptFromFlandersAnnualTax() {
            VehicleData vehicle = new VehicleData(
                    150,
                    10,
                    FuelType.electric,
                    EuroNorm.euro_6d,
                    0,
                    null,
                    2000
            );

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicle,
                    Region.flanders,
                    TaxType.annual,
                    BRUSSELS_DATE
            );

            assertThat(response.getIsExempt()).isTrue();
            assertThat(response.getAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }
}
