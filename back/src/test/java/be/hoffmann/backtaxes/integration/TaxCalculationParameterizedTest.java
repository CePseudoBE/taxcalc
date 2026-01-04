package be.hoffmann.backtaxes.integration;

import be.hoffmann.backtaxes.dto.response.TaxCalculationResponse;
import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.service.TaxCalculationService;
import be.hoffmann.backtaxes.service.TaxCalculationService.VehicleData;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
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
 * Parameterized tests for tax calculations using CSV test cases.
 * Each row in the CSV file represents a test scenario with expected results.
 *
 * This allows easy addition of new test cases by simply adding rows to the CSV,
 * including cases validated against official Belgian tax simulators.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("integration")
@DisplayName("Tax Calculation - Parameterized Tests")
class TaxCalculationParameterizedTest {

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

    @ParameterizedTest(name = "{0}")
    @CsvFileSource(
            resources = "/tax-calculation-test-cases.csv",
            numLinesToSkip = 1,
            delimiter = ',',
            nullValues = ""
    )
    @DisplayName("Tax calculation validation")
    void validateTaxCalculation(
            String description,
            String regionStr,
            String taxTypeStr,
            int powerKw,
            int fiscalHp,
            String fuelTypeStr,
            String euroNormStr,
            Integer co2Wltp,
            Integer mmaKg,
            int vehicleAge,
            boolean isNew,
            double expectedMin,
            double expectedMax,
            boolean isExempt
    ) {
        // Skip comment lines
        if (description == null || description.startsWith("#")) {
            return;
        }

        // Parse enums
        Region region = Region.valueOf(regionStr);
        TaxType taxType = TaxType.valueOf(taxTypeStr);
        FuelType fuelType = FuelType.valueOf(fuelTypeStr);
        EuroNorm euroNorm = EuroNorm.valueOf(euroNormStr);

        // Create vehicle data
        VehicleData vehicle = new VehicleData(
                powerKw,
                fiscalHp,
                fuelType,
                euroNorm,
                co2Wltp,
                null,  // co2Nedc
                mmaKg
        );

        // Use current date as reference for age calculation
        // The tax rates are looked up based on LocalDate.now() in the service
        LocalDate referenceDate = LocalDate.now();

        // Calculate registration date based on vehicle age
        LocalDate registrationDate = isNew
                ? referenceDate
                : referenceDate.minusYears(vehicleAge);

        // Calculate tax
        TaxCalculationResponse response = taxCalculationService.calculateTax(
                vehicle,
                region,
                taxType,
                registrationDate
        );

        // Assertions
        if (isExempt) {
            assertThat(response.getIsExempt())
                    .as("Vehicle should be exempt: %s", description)
                    .isTrue();
            assertThat(response.getAmount())
                    .as("Exempt vehicle amount should be zero: %s", description)
                    .isEqualByComparingTo(BigDecimal.ZERO);
        } else {
            assertThat(response.getAmount())
                    .as("Amount should be >= %s for: %s", expectedMin, description)
                    .isGreaterThanOrEqualTo(BigDecimal.valueOf(expectedMin));
            assertThat(response.getAmount())
                    .as("Amount should be <= %s for: %s", expectedMax, description)
                    .isLessThanOrEqualTo(BigDecimal.valueOf(expectedMax));
        }
    }
}
