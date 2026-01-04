package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.request.FirstRegistrationDate;
import be.hoffmann.backtaxes.dto.request.TaxCalculationRequest;
import be.hoffmann.backtaxes.dto.response.TaxCalculationResponse;
import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.Variant;
import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.repository.VehicleSubmissionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxCalculationServiceTest {

    @Mock
    private VariantService variantService;

    @Mock
    private VehicleSubmissionRepository submissionRepository;

    @Mock
    private TaxConfigService taxConfigService;

    private TaxCalculationService taxCalculationService;

    @BeforeEach
    void setUp() {
        taxCalculationService = new TaxCalculationService(
                variantService, submissionRepository, taxConfigService);
    }

    @Nested
    @DisplayName("calculateBoth")
    class CalculateBothTests {

        @Test
        @DisplayName("should throw exception when neither variantId nor submissionId provided")
        void shouldThrowWhenNoVehicleReference() {
            TaxCalculationRequest request = new TaxCalculationRequest();
            request.setRegion(Region.wallonia);

            assertThatThrownBy(() -> taxCalculationService.calculateBoth(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("variantId or submissionId");
        }

        @Test
        @DisplayName("should throw exception when both variantId and submissionId provided")
        void shouldThrowWhenBothVehicleReferences() {
            TaxCalculationRequest request = new TaxCalculationRequest();
            request.setVariantId(1L);
            request.setSubmissionId(2L);
            request.setRegion(Region.wallonia);

            assertThatThrownBy(() -> taxCalculationService.calculateBoth(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("variantId or submissionId");
        }

        @Test
        @DisplayName("should throw exception when region is null")
        void shouldThrowWhenRegionNull() {
            TaxCalculationRequest request = new TaxCalculationRequest();
            request.setVariantId(1L);

            assertThatThrownBy(() -> taxCalculationService.calculateBoth(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Region is required");
        }

        @Test
        @DisplayName("should calculate both TMC and annual taxes")
        void shouldCalculateBothTaxes() {
            TaxCalculationRequest request = new TaxCalculationRequest();
            request.setVariantId(1L);
            request.setRegion(Region.wallonia);
            request.setFirstRegistrationDate(new FirstRegistrationDate(2024, 1));

            Variant variant = createTestVariant();
            when(variantService.findById(1L)).thenReturn(variant);

            // TMC mocks
            when(taxConfigService.isZeroEmissionExempt(eq(Region.wallonia), eq(TaxType.tmc), any(), any())).thenReturn(false);
            when(taxConfigService.getAgeCoefficient(eq(Region.wallonia), eq(TaxType.tmc), anyInt(), any(), any())).thenReturn(BigDecimal.ONE);
            when(taxConfigService.findBracket(eq(Region.wallonia), eq(TaxType.tmc), eq("power_kw"), any(Integer.class), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(500))));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("energy_thermal"), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("co2_reference_wltp"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(136));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("mma_reference"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1838));
            when(taxConfigService.getMinAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(BigDecimal.valueOf(61.5));
            when(taxConfigService.getMaxAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(Optional.empty());

            // Annual mocks
            when(taxConfigService.isZeroEmissionExempt(eq(Region.wallonia), eq(TaxType.annual), any(), any())).thenReturn(false);
            when(taxConfigService.findBracket(eq(Region.wallonia), eq(TaxType.annual), eq("fiscal_hp"), any(Integer.class), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(300))));
            when(taxConfigService.getMinAmount(eq(Region.wallonia), eq(TaxType.annual), any())).thenReturn(BigDecimal.valueOf(87));
            when(taxConfigService.getMaxAmount(eq(Region.wallonia), eq(TaxType.annual), any())).thenReturn(Optional.empty());

            TaxCalculationService.TaxCalculationResult result = taxCalculationService.calculateBoth(request);

            assertThat(result.getTmc()).isNotNull();
            assertThat(result.getAnnual()).isNotNull();
            assertThat(result.getTmc().getRegion()).isEqualTo(Region.wallonia);
            assertThat(result.getAnnual().getRegion()).isEqualTo(Region.wallonia);
            assertThat(result.getTmc().getTaxType()).isEqualTo(TaxType.tmc);
            assertThat(result.getAnnual().getTaxType()).isEqualTo(TaxType.annual);
        }
    }

    @Nested
    @DisplayName("Electric vehicle exemptions")
    class ExemptionTests {

        @Test
        @DisplayName("should exempt electric vehicles")
        void shouldExemptElectricVehicles() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    150, 10, FuelType.electric, EuroNorm.euro_6d, null, null, null);

            when(taxConfigService.isZeroEmissionExempt(
                    eq(Region.wallonia), eq(TaxType.tmc), eq(FuelType.electric), any()))
                    .thenReturn(true);

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.wallonia, TaxType.tmc, LocalDate.now());

            assertThat(response.getIsExempt()).isTrue();
            assertThat(response.getAmount()).isEqualTo(BigDecimal.ZERO);
            assertThat(response.getExemptionReason()).contains("electric");
        }

        @Test
        @DisplayName("should exempt hydrogen vehicles")
        void shouldExemptHydrogenVehicles() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    200, 15, FuelType.hydrogen, EuroNorm.euro_6d, null, null, null);

            when(taxConfigService.isZeroEmissionExempt(
                    eq(Region.flanders), eq(TaxType.annual), eq(FuelType.hydrogen), any()))
                    .thenReturn(true);

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.flanders, TaxType.annual, LocalDate.now());

            assertThat(response.getIsExempt()).isTrue();
            assertThat(response.getAmount()).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Wallonia TMC calculation")
    class WalloniaTmcTests {

        @Test
        @DisplayName("should calculate TMC with CO2 factor")
        void shouldCalculateTmcWithCo2Factor() {
            // Include MMA so the mma_reference mock is used
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    110, 8, FuelType.petrol, EuroNorm.euro_6d, 150, null, 1800);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.getAgeCoefficient(eq(Region.wallonia), eq(TaxType.tmc), eq(0), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.findBracket(eq(Region.wallonia), eq(TaxType.tmc), eq("power_kw"), any(Integer.class), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(500))));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("energy_thermal"), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("co2_reference_wltp"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(136));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("mma_reference"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1838));
            when(taxConfigService.getMinAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(BigDecimal.valueOf(61.5));
            when(taxConfigService.getMaxAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(Optional.empty());

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.wallonia, TaxType.tmc, LocalDate.now());

            assertThat(response.getIsExempt()).isFalse();
            assertThat(response.getAmount()).isNotNull();
            // The new formula uses CO2/reference, so look for breakdown containing CO2 info
            assertThat(response.getAmount()).isGreaterThan(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should calculate TMC with MMA factor")
        void shouldCalculateTmcWithMmaFactor() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    150, 10, FuelType.diesel, EuroNorm.euro_6d, 140, null, 2000);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.getAgeCoefficient(eq(Region.wallonia), eq(TaxType.tmc), eq(0), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.findBracket(eq(Region.wallonia), eq(TaxType.tmc), eq("power_kw"), any(Integer.class), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(500))));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("energy_thermal"), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("co2_reference_wltp"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(136));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("mma_reference"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1838));
            when(taxConfigService.getMinAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(BigDecimal.valueOf(61.5));
            when(taxConfigService.getMaxAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(Optional.empty());

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.wallonia, TaxType.tmc, LocalDate.now());

            // The new formula uses MMA/reference, verify amount is calculated
            assertThat(response.getAmount()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Brussels TMC calculation")
    class BrusselsTmcTests {

        @Test
        @DisplayName("should apply LPG reduction")
        void shouldApplyLpgReduction() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    100, 8, FuelType.lpg, EuroNorm.euro_6d, null, null, null);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.getAgeCoefficient(eq(Region.brussels), eq(TaxType.tmc), eq(0), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.findBracket(eq(Region.brussels), eq(TaxType.tmc), eq("fiscal_hp"), any(Integer.class), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(800))));
            when(taxConfigService.getParameter(eq(Region.brussels), eq(TaxType.tmc), eq("lpg_reduction"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(298));
            when(taxConfigService.getMinAmount(eq(Region.brussels), eq(TaxType.tmc), any())).thenReturn(BigDecimal.valueOf(61.5));
            when(taxConfigService.getMaxAmount(eq(Region.brussels), eq(TaxType.tmc), any())).thenReturn(Optional.empty());

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.brussels, TaxType.tmc, LocalDate.now());

            assertThat(response.getBreakdown()).containsKey("lpgReduction");
        }
    }

    @Nested
    @DisplayName("Flanders TMC calculation")
    class FlandersTmcTests {

        @Test
        @DisplayName("should apply luchtcomponent based on Euro norm")
        void shouldApplyLuchtcomponent() {
            // Véhicule essence Euro 5 avec CO2 connu
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    120, 9, FuelType.petrol, EuroNorm.euro_5, 150, null, null);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.getAgeCoefficient(eq(Region.flanders), eq(TaxType.tmc), eq(0), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            // Paramètres pour la formule officielle WLTP (2026 = véhicule WLTP)
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.tmc), eq("formula_divisor"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(246));
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.tmc), eq("formula_constant"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(4500));
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.tmc), eq("fuel_factor_default"), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            // WLTP q factor for 2026+ vehicles (no co2_correction_x needed for WLTP)
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.tmc), eq("wltp_q_factor"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1.245));
            // Euro 5 = numeric value 5, luchtcomponent petrol
            when(taxConfigService.findBracket(eq(Region.flanders), eq(TaxType.tmc), eq("luchtcomponent_petrol"), eq(5), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(113.33))));
            when(taxConfigService.getMinAmount(eq(Region.flanders), eq(TaxType.tmc), any())).thenReturn(BigDecimal.valueOf(55.88));
            when(taxConfigService.getMaxAmount(eq(Region.flanders), eq(TaxType.tmc), any())).thenReturn(Optional.of(BigDecimal.valueOf(13969.29)));

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.flanders, TaxType.tmc, LocalDate.now());

            assertThat(response.getBreakdown()).containsKey("luchtcomponent_c");
            assertThat(response.getBreakdown()).containsKey("euroNorm");
        }

        @Test
        @DisplayName("should apply official BIV formula with CO2 (WLTP for 2021+ vehicles)")
        void shouldApplyOfficialBivFormula() {
            // Véhicule essence Euro 6 avec CO2 faible
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    80, 6, FuelType.petrol, EuroNorm.euro_6d, 120, null, null);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.getAgeCoefficient(eq(Region.flanders), eq(TaxType.tmc), eq(0), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            // Paramètres formule officielle
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.tmc), eq("formula_divisor"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(246));
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.tmc), eq("formula_constant"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(4500));
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.tmc), eq("fuel_factor_default"), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            // WLTP q factor for 2026+ vehicles
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.tmc), eq("wltp_q_factor"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1.245));
            // Euro 6 = numeric value 6, luchtcomponent petrol
            when(taxConfigService.findBracket(eq(Region.flanders), eq(TaxType.tmc), eq("luchtcomponent_petrol"), eq(6), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(27.43))));
            when(taxConfigService.getMinAmount(eq(Region.flanders), eq(TaxType.tmc), any())).thenReturn(BigDecimal.valueOf(55.88));
            when(taxConfigService.getMaxAmount(eq(Region.flanders), eq(TaxType.tmc), any())).thenReturn(Optional.of(BigDecimal.valueOf(13969.29)));

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.flanders, TaxType.tmc, LocalDate.now());

            // Vérifier que la formule WLTP officielle est utilisée (q factor multiplicatif)
            assertThat(response.getBreakdown()).containsKey("step1_co2_f_q");
            assertThat(response.getBreakdown()).containsKey("step3_power6");
            assertThat(response.getBreakdown()).containsKey("luchtcomponent_c");
            assertThat(response.getBreakdown().get("formulaType")).isEqualTo("WLTP");
        }
    }

    @Nested
    @DisplayName("Annual tax calculation")
    class AnnualTaxTests {

        @Test
        @DisplayName("should calculate annual tax based on fiscal HP")
        void shouldCalculateBasedOnFiscalHp() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    100, 8, FuelType.petrol, EuroNorm.euro_6d, null, null, null);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.findBracket(eq(Region.wallonia), eq(TaxType.annual), eq("fiscal_hp"), any(Integer.class), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(300))));
            when(taxConfigService.getMinAmount(eq(Region.wallonia), eq(TaxType.annual), any())).thenReturn(BigDecimal.valueOf(87));
            when(taxConfigService.getMaxAmount(eq(Region.wallonia), eq(TaxType.annual), any())).thenReturn(Optional.empty());

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.wallonia, TaxType.annual, LocalDate.now());

            assertThat(response.getBreakdown()).containsKey("fiscalHp");
            assertThat(response.getBreakdown().get("fiscalHp")).isEqualTo(8);
        }

        @Test
        @DisplayName("should add LPG supplement")
        void shouldAddLpgSupplement() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    100, 8, FuelType.lpg, EuroNorm.euro_6d, null, null, null);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.findBracket(eq(Region.brussels), eq(TaxType.annual), eq("fiscal_hp"), any(Integer.class), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(300))));
            when(taxConfigService.getParameter(eq(Region.brussels), eq(TaxType.annual), eq("lpg_supplement_per_hp"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(99.16));
            when(taxConfigService.getMinAmount(eq(Region.brussels), eq(TaxType.annual), any())).thenReturn(BigDecimal.valueOf(87));
            when(taxConfigService.getMaxAmount(eq(Region.brussels), eq(TaxType.annual), any())).thenReturn(Optional.empty());

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.brussels, TaxType.annual, LocalDate.now());

            assertThat(response.getBreakdown()).containsKey("lpgSupplement");
        }

        @Test
        @DisplayName("should apply CO2 adjustment for Flanders")
        void shouldApplyCo2AdjustmentForFlanders() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    120, 10, FuelType.diesel, EuroNorm.euro_6d, 160, null, null);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.findBracket(eq(Region.flanders), eq(TaxType.annual), eq("fiscal_hp"), any(Integer.class), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(300))));
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.annual), eq("co2_reference_wltp"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(149));
            when(taxConfigService.getParameter(eq(Region.flanders), eq(TaxType.annual), eq("co2_correction_percent"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(0.003));
            when(taxConfigService.getMinAmount(eq(Region.flanders), eq(TaxType.annual), any())).thenReturn(BigDecimal.valueOf(87));
            when(taxConfigService.getMaxAmount(eq(Region.flanders), eq(TaxType.annual), any())).thenReturn(Optional.empty());

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.flanders, TaxType.annual, LocalDate.now());

            assertThat(response.getBreakdown()).containsKey("co2Adjustment");
        }
    }

    @Nested
    @DisplayName("Min/Max amount limits")
    class AmountLimitsTests {

        @Test
        @DisplayName("should apply minimum amount when calculated is lower")
        void shouldApplyMinAmount() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    50, 3, FuelType.petrol, EuroNorm.euro_6d, 100, null, 1500);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.getAgeCoefficient(eq(Region.wallonia), eq(TaxType.tmc), eq(0), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.findBracket(eq(Region.wallonia), eq(TaxType.tmc), eq("power_kw"), eq(50), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(10))));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("energy_thermal"), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("co2_reference_wltp"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(136));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("mma_reference"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1838));
            when(taxConfigService.getMinAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(BigDecimal.valueOf(100));
            when(taxConfigService.getMaxAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(Optional.empty());

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.wallonia, TaxType.tmc, LocalDate.now());

            assertThat(response.getAmount()).isGreaterThanOrEqualTo(BigDecimal.valueOf(100));
            assertThat(response.getBreakdown()).containsKey("minAmountApplied");
        }

        @Test
        @DisplayName("should apply maximum amount when calculated is higher")
        void shouldApplyMaxAmount() {
            TaxCalculationService.VehicleData vehicleData = new TaxCalculationService.VehicleData(
                    400, 25, FuelType.diesel, EuroNorm.euro_4, 250, null, 3000);

            when(taxConfigService.isZeroEmissionExempt(any(), any(), any(), any())).thenReturn(false);
            when(taxConfigService.getAgeCoefficient(eq(Region.wallonia), eq(TaxType.tmc), eq(0), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.findBracket(eq(Region.wallonia), eq(TaxType.tmc), eq("power_kw"), eq(400), any()))
                    .thenReturn(Optional.of(createTaxBracket(BigDecimal.valueOf(50000))));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("energy_thermal"), any(), any()))
                    .thenReturn(BigDecimal.ONE);
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("co2_reference_wltp"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(136));
            when(taxConfigService.getParameter(eq(Region.wallonia), eq(TaxType.tmc), eq("mma_reference"), any(), any()))
                    .thenReturn(BigDecimal.valueOf(1838));
            when(taxConfigService.getMinAmount(eq(Region.wallonia), eq(TaxType.tmc), any())).thenReturn(BigDecimal.ZERO);
            when(taxConfigService.getMaxAmount(eq(Region.wallonia), eq(TaxType.tmc), any()))
                    .thenReturn(Optional.of(BigDecimal.valueOf(20000)));

            TaxCalculationResponse response = taxCalculationService.calculateTax(
                    vehicleData, Region.wallonia, TaxType.tmc, LocalDate.now());

            assertThat(response.getAmount()).isLessThanOrEqualTo(BigDecimal.valueOf(20000));
            assertThat(response.getBreakdown()).containsKey("maxAmountApplied");
        }
    }

    private Variant createTestVariant() {
        Variant variant = new Variant();
        variant.setId(1L);
        variant.setPowerKw(110);
        variant.setFiscalHp(8);
        variant.setFuel(FuelType.petrol);
        variant.setEuroNorm(EuroNorm.euro_6d);
        variant.setCo2Wltp(140);
        variant.setMmaKg(1800);  // Add MMA so the mma_reference mock is used
        return variant;
    }

    private TaxBracket createTaxBracket(BigDecimal amount) {
        TaxBracket bracket = new TaxBracket();
        bracket.setAmount(amount);
        return bracket;
    }
}
