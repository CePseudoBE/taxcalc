package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.AgeCoefficient;
import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.TaxParameter;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.repository.AgeCoefficientRepository;
import be.hoffmann.backtaxes.repository.TaxBracketRepository;
import be.hoffmann.backtaxes.repository.TaxExemptionRepository;
import be.hoffmann.backtaxes.repository.TaxParameterRepository;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxConfigServiceTest {

    @Mock
    private TaxBracketRepository taxBracketRepository;

    @Mock
    private TaxParameterRepository taxParameterRepository;

    @Mock
    private AgeCoefficientRepository ageCoefficientRepository;

    @Mock
    private TaxExemptionRepository taxExemptionRepository;

    private TaxConfigService taxConfigService;

    @BeforeEach
    void setUp() {
        taxConfigService = new TaxConfigService(
                taxBracketRepository, taxParameterRepository,
                ageCoefficientRepository, taxExemptionRepository);
    }

    @Nested
    @DisplayName("findBracket")
    class FindBracketTests {

        @Test
        @DisplayName("should return bracket when found")
        void shouldReturnBracketWhenFound() {
            TaxBracket bracket = createTaxBracket(BigDecimal.valueOf(500));
            when(taxBracketRepository.findMatchingBracket(
                    Region.wallonia, TaxType.tmc, "power_kw", 110, LocalDate.of(2024, 1, 1)))
                    .thenReturn(Optional.of(bracket));

            Optional<TaxBracket> result = taxConfigService.findBracket(
                    Region.wallonia, TaxType.tmc, "power_kw", 110, LocalDate.of(2024, 1, 1));

            assertThat(result).isPresent();
            assertThat(result.get().getAmount()).isEqualTo(BigDecimal.valueOf(500));
        }

        @Test
        @DisplayName("should return empty when bracket not found")
        void shouldReturnEmptyWhenNotFound() {
            when(taxBracketRepository.findMatchingBracket(any(), any(), any(), anyInt(), any()))
                    .thenReturn(Optional.empty());

            Optional<TaxBracket> result = taxConfigService.findBracket(
                    Region.flanders, TaxType.annual, "fiscal_hp", 99, LocalDate.now());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("getParameter")
    class GetParameterTests {

        @Test
        @DisplayName("should return parameter value when found")
        void shouldReturnParameterWhenFound() {
            TaxParameter param = createTaxParameter(BigDecimal.valueOf(136));
            when(taxParameterRepository.findValidParameter(
                    Region.wallonia, TaxType.tmc, "co2_reference_wltp", LocalDate.of(2024, 1, 1)))
                    .thenReturn(Optional.of(param));

            Optional<BigDecimal> result = taxConfigService.getParameter(
                    Region.wallonia, TaxType.tmc, "co2_reference_wltp", LocalDate.of(2024, 1, 1));

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(BigDecimal.valueOf(136));
        }

        @Test
        @DisplayName("should return default value when parameter not found")
        void shouldReturnDefaultWhenNotFound() {
            when(taxParameterRepository.findValidParameter(any(), any(), any(), any()))
                    .thenReturn(Optional.empty());

            BigDecimal result = taxConfigService.getParameter(
                    Region.brussels, TaxType.annual, "unknown_param",
                    LocalDate.now(), BigDecimal.valueOf(42));

            assertThat(result).isEqualTo(BigDecimal.valueOf(42));
        }

        @Test
        @DisplayName("should return found value instead of default")
        void shouldReturnFoundValueInsteadOfDefault() {
            TaxParameter param = createTaxParameter(BigDecimal.valueOf(100));
            when(taxParameterRepository.findValidParameter(any(), any(), any(), any()))
                    .thenReturn(Optional.of(param));

            BigDecimal result = taxConfigService.getParameter(
                    Region.flanders, TaxType.tmc, "some_param",
                    LocalDate.now(), BigDecimal.valueOf(999));

            assertThat(result).isEqualTo(BigDecimal.valueOf(100));
        }
    }

    @Nested
    @DisplayName("getAgeCoefficient")
    class GetAgeCoefficientTests {

        @Test
        @DisplayName("should return coefficient when found")
        void shouldReturnCoefficientWhenFound() {
            AgeCoefficient coef = createAgeCoefficient(BigDecimal.valueOf(0.85));
            when(ageCoefficientRepository.findValidCoefficient(
                    Region.wallonia, TaxType.tmc, 3, LocalDate.of(2024, 1, 1)))
                    .thenReturn(Optional.of(coef));

            Optional<BigDecimal> result = taxConfigService.getAgeCoefficient(
                    Region.wallonia, TaxType.tmc, 3, LocalDate.of(2024, 1, 1));

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(BigDecimal.valueOf(0.85));
        }

        @Test
        @DisplayName("should cap vehicle age at 15 years")
        void shouldCapAgeAt15Years() {
            AgeCoefficient coef = createAgeCoefficient(BigDecimal.valueOf(0.10));
            when(ageCoefficientRepository.findValidCoefficient(
                    eq(Region.flanders), eq(TaxType.tmc), eq(15), any()))
                    .thenReturn(Optional.of(coef));

            taxConfigService.getAgeCoefficient(
                    Region.flanders, TaxType.tmc, 25, LocalDate.now());

            verify(ageCoefficientRepository).findValidCoefficient(
                    eq(Region.flanders), eq(TaxType.tmc), eq(15), any());
        }

        @Test
        @DisplayName("should return default value when coefficient not found")
        void shouldReturnDefaultWhenNotFound() {
            when(ageCoefficientRepository.findValidCoefficient(any(), any(), anyInt(), any()))
                    .thenReturn(Optional.empty());

            BigDecimal result = taxConfigService.getAgeCoefficient(
                    Region.brussels, TaxType.annual, 5, LocalDate.now(), BigDecimal.ONE);

            assertThat(result).isEqualTo(BigDecimal.ONE);
        }
    }

    @Nested
    @DisplayName("isExempt")
    class IsExemptTests {

        @Test
        @DisplayName("should return true when exemption exists")
        void shouldReturnTrueWhenExemptionExists() {
            when(taxExemptionRepository.isExempt(
                    Region.wallonia, TaxType.tmc, "fuel_electric", LocalDate.of(2024, 1, 1)))
                    .thenReturn(true);

            boolean result = taxConfigService.isExempt(
                    Region.wallonia, TaxType.tmc, FuelType.electric, LocalDate.of(2024, 1, 1));

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false when no exemption")
        void shouldReturnFalseWhenNoExemption() {
            when(taxExemptionRepository.isExempt(any(), any(), any(), any()))
                    .thenReturn(false);

            boolean result = taxConfigService.isExempt(
                    Region.flanders, TaxType.annual, FuelType.diesel, LocalDate.now());

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isZeroEmissionExempt")
    class IsZeroEmissionExemptTests {

        @Test
        @DisplayName("should check exemption for electric vehicles")
        void shouldCheckExemptionForElectric() {
            when(taxExemptionRepository.isExempt(
                    Region.wallonia, TaxType.tmc, "fuel_electric", LocalDate.of(2024, 1, 1)))
                    .thenReturn(true);

            boolean result = taxConfigService.isZeroEmissionExempt(
                    Region.wallonia, TaxType.tmc, FuelType.electric, LocalDate.of(2024, 1, 1));

            assertThat(result).isTrue();
            verify(taxExemptionRepository).isExempt(
                    Region.wallonia, TaxType.tmc, "fuel_electric", LocalDate.of(2024, 1, 1));
        }

        @Test
        @DisplayName("should check exemption for hydrogen vehicles")
        void shouldCheckExemptionForHydrogen() {
            when(taxExemptionRepository.isExempt(
                    Region.flanders, TaxType.annual, "fuel_hydrogen", LocalDate.now()))
                    .thenReturn(true);

            boolean result = taxConfigService.isZeroEmissionExempt(
                    Region.flanders, TaxType.annual, FuelType.hydrogen, LocalDate.now());

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("should return false for non-zero-emission vehicles")
        void shouldReturnFalseForNonZeroEmission() {
            boolean resultDiesel = taxConfigService.isZeroEmissionExempt(
                    Region.brussels, TaxType.tmc, FuelType.diesel, LocalDate.now());
            boolean resultPetrol = taxConfigService.isZeroEmissionExempt(
                    Region.brussels, TaxType.tmc, FuelType.petrol, LocalDate.now());
            boolean resultHybrid = taxConfigService.isZeroEmissionExempt(
                    Region.brussels, TaxType.tmc, FuelType.hybrid_petrol, LocalDate.now());

            assertThat(resultDiesel).isFalse();
            assertThat(resultPetrol).isFalse();
            assertThat(resultHybrid).isFalse();
        }
    }

    @Nested
    @DisplayName("getMinAmount")
    class GetMinAmountTests {

        @Test
        @DisplayName("should return min amount from parameter")
        void shouldReturnMinAmount() {
            TaxParameter param = createTaxParameter(BigDecimal.valueOf(61.5));
            when(taxParameterRepository.findValidParameter(
                    Region.wallonia, TaxType.tmc, "min_amount", LocalDate.of(2024, 1, 1)))
                    .thenReturn(Optional.of(param));

            BigDecimal result = taxConfigService.getMinAmount(
                    Region.wallonia, TaxType.tmc, LocalDate.of(2024, 1, 1));

            assertThat(result).isEqualTo(BigDecimal.valueOf(61.5));
        }

        @Test
        @DisplayName("should return zero when min amount not configured")
        void shouldReturnZeroWhenNotConfigured() {
            when(taxParameterRepository.findValidParameter(any(), any(), eq("min_amount"), any()))
                    .thenReturn(Optional.empty());

            BigDecimal result = taxConfigService.getMinAmount(
                    Region.flanders, TaxType.annual, LocalDate.now());

            assertThat(result).isEqualTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getMaxAmount")
    class GetMaxAmountTests {

        @Test
        @DisplayName("should return max amount when configured")
        void shouldReturnMaxAmount() {
            TaxParameter param = createTaxParameter(BigDecimal.valueOf(20000));
            when(taxParameterRepository.findValidParameter(
                    Region.brussels, TaxType.tmc, "max_amount", LocalDate.of(2024, 1, 1)))
                    .thenReturn(Optional.of(param));

            Optional<BigDecimal> result = taxConfigService.getMaxAmount(
                    Region.brussels, TaxType.tmc, LocalDate.of(2024, 1, 1));

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(BigDecimal.valueOf(20000));
        }

        @Test
        @DisplayName("should return empty when max amount not configured")
        void shouldReturnEmptyWhenNotConfigured() {
            when(taxParameterRepository.findValidParameter(any(), any(), eq("max_amount"), any()))
                    .thenReturn(Optional.empty());

            Optional<BigDecimal> result = taxConfigService.getMaxAmount(
                    Region.wallonia, TaxType.annual, LocalDate.now());

            assertThat(result).isEmpty();
        }
    }

    private TaxBracket createTaxBracket(BigDecimal amount) {
        TaxBracket bracket = new TaxBracket();
        bracket.setAmount(amount);
        return bracket;
    }

    private TaxParameter createTaxParameter(BigDecimal value) {
        TaxParameter param = new TaxParameter();
        param.setParamValue(value);
        return param;
    }

    private AgeCoefficient createAgeCoefficient(BigDecimal coefficient) {
        AgeCoefficient coef = new AgeCoefficient();
        coef.setCoefficient(coefficient);
        return coef;
    }
}
