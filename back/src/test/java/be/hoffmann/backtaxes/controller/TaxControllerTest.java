package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.response.TaxCalculationResponse;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.exception.GlobalExceptionHandler;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.service.AnalyticsService;
import be.hoffmann.backtaxes.service.TaxCalculationService;
import be.hoffmann.backtaxes.service.TaxCalculationService.TaxCalculationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaxControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaxCalculationService taxCalculationService;

    @Mock
    private AnalyticsService analyticsService;

    @BeforeEach
    void setUp() {
        TaxController controller = new TaxController(taxCalculationService, analyticsService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    private void setupAnalyticsMocks() {
        when(analyticsService.getOrCreateSessionId(any(), any())).thenReturn(UUID.randomUUID());
        when(analyticsService.builder()).thenReturn(new AnalyticsService.SearchEventBuilder());
    }

    @Nested
    @DisplayName("POST /api/tax/calculate")
    class CalculateTaxTests {

        @Test
        @DisplayName("should return both TMC and annual taxes")
        void shouldReturnBothTaxes() throws Exception {
            setupAnalyticsMocks();
            TaxCalculationResponse tmc = createTaxResponse(Region.wallonia, TaxType.tmc, new BigDecimal("1500.00"));
            TaxCalculationResponse annual = createTaxResponse(Region.wallonia, TaxType.annual, new BigDecimal("300.00"));
            TaxCalculationResult result = new TaxCalculationResult(tmc, annual);

            when(taxCalculationService.calculateBoth(any())).thenReturn(result);

            mockMvc.perform(post("/api/tax/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "variantId": 1,
                                    "region": "wallonia",
                                    "firstRegistrationDate": {
                                        "year": 2024,
                                        "month": 1
                                    }
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tmc.amount", is(1500.0)))
                    .andExpect(jsonPath("$.data.tmc.region", is("wallonia")))
                    .andExpect(jsonPath("$.data.tmc.taxType", is("tmc")))
                    .andExpect(jsonPath("$.data.annual.amount", is(300.0)))
                    .andExpect(jsonPath("$.data.annual.region", is("wallonia")))
                    .andExpect(jsonPath("$.data.annual.taxType", is("annual")));

            verify(taxCalculationService).calculateBoth(any());
        }

        @Test
        @DisplayName("should return exempt response for electric vehicle")
        void shouldReturnExemptForElectric() throws Exception {
            setupAnalyticsMocks();
            TaxCalculationResponse tmc = createExemptResponse(Region.flanders, TaxType.tmc, "Zero emission vehicle (electric)");
            TaxCalculationResponse annual = createExemptResponse(Region.flanders, TaxType.annual, "Zero emission vehicle (electric)");
            TaxCalculationResult result = new TaxCalculationResult(tmc, annual);

            when(taxCalculationService.calculateBoth(any())).thenReturn(result);

            mockMvc.perform(post("/api/tax/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "variantId": 2,
                                    "region": "flanders",
                                    "firstRegistrationDate": {
                                        "year": 2024,
                                        "month": 6
                                    }
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tmc.amount", is(0)))
                    .andExpect(jsonPath("$.data.tmc.isExempt", is(true)))
                    .andExpect(jsonPath("$.data.tmc.exemptionReason", containsString("electric")));
        }

        @Test
        @DisplayName("should return 400 when region is missing")
        void shouldReturn400WhenRegionMissing() throws Exception {
            // No mock needed - Spring validation handles @NotNull on region
            mockMvc.perform(post("/api/tax/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "variantId": 1,
                                    "firstRegistrationDate": {
                                        "year": 2024,
                                        "month": 1
                                    }
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should return 400 when both variantId and submissionId provided")
        void shouldReturn400WhenBothVehicleReferences() throws Exception {
            // With @AssertTrue validation on isValidVehicleReference, the request
            // is rejected by Spring validation before reaching the service
            mockMvc.perform(post("/api/tax/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "variantId": 1,
                                    "submissionId": 2,
                                    "region": "wallonia",
                                    "firstRegistrationDate": {
                                        "year": 2024,
                                        "month": 1
                                    }
                                }
                                """))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("should accept submissionId instead of variantId")
        void shouldAcceptSubmissionId() throws Exception {
            setupAnalyticsMocks();
            TaxCalculationResponse tmc = createTaxResponse(Region.brussels, TaxType.tmc, new BigDecimal("800.00"));
            TaxCalculationResponse annual = createTaxResponse(Region.brussels, TaxType.annual, new BigDecimal("250.00"));
            TaxCalculationResult result = new TaxCalculationResult(tmc, annual);

            when(taxCalculationService.calculateBoth(any())).thenReturn(result);

            mockMvc.perform(post("/api/tax/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "submissionId": 5,
                                    "region": "brussels",
                                    "firstRegistrationDate": {
                                        "year": 2020,
                                        "month": 3
                                    }
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.tmc.region", is("brussels")));
        }
    }

    @Nested
    @DisplayName("POST /api/tax/tmc")
    class CalculateTmcTests {

        @Test
        @DisplayName("should return only TMC")
        void shouldReturnOnlyTmc() throws Exception {
            setupAnalyticsMocks();
            TaxCalculationResponse tmc = createTaxResponse(Region.wallonia, TaxType.tmc, new BigDecimal("2000.00"));

            when(taxCalculationService.calculateTmcOnly(any())).thenReturn(tmc);

            mockMvc.perform(post("/api/tax/tmc")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "variantId": 1,
                                    "region": "wallonia",
                                    "firstRegistrationDate": {
                                        "year": 2024,
                                        "month": 1
                                    }
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.amount", is(2000.0)))
                    .andExpect(jsonPath("$.data.taxType", is("tmc")))
                    .andExpect(jsonPath("$.data.region", is("wallonia")));
        }
    }

    @Nested
    @DisplayName("POST /api/tax/annual")
    class CalculateAnnualTests {

        @Test
        @DisplayName("should return only annual tax")
        void shouldReturnOnlyAnnual() throws Exception {
            setupAnalyticsMocks();
            TaxCalculationResponse annual = createTaxResponse(Region.flanders, TaxType.annual, new BigDecimal("350.00"));

            when(taxCalculationService.calculateAnnualOnly(any())).thenReturn(annual);

            mockMvc.perform(post("/api/tax/annual")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "variantId": 1,
                                    "region": "flanders",
                                    "firstRegistrationDate": {
                                        "year": 2024,
                                        "month": 1
                                    }
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.amount", is(350.0)))
                    .andExpect(jsonPath("$.data.taxType", is("annual")))
                    .andExpect(jsonPath("$.data.region", is("flanders")));
        }
    }

    @Nested
    @DisplayName("Analytics integration")
    class AnalyticsTests {

        @Test
        @DisplayName("should log analytics event on calculation")
        void shouldLogAnalyticsEvent() throws Exception {
            setupAnalyticsMocks();
            TaxCalculationResponse tmc = createTaxResponse(Region.wallonia, TaxType.tmc, new BigDecimal("1000.00"));
            TaxCalculationResponse annual = createTaxResponse(Region.wallonia, TaxType.annual, new BigDecimal("200.00"));
            TaxCalculationResult result = new TaxCalculationResult(tmc, annual);

            when(taxCalculationService.calculateBoth(any())).thenReturn(result);

            mockMvc.perform(post("/api/tax/calculate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("User-Agent", "Mozilla/5.0")
                            .header("Accept-Language", "fr-BE")
                            .content("""
                                {
                                    "variantId": 1,
                                    "region": "wallonia",
                                    "firstRegistrationDate": {
                                        "year": 2024,
                                        "month": 1
                                    }
                                }
                                """))
                    .andExpect(status().isOk());

            verify(analyticsService).getOrCreateSessionId(any(), any());
            verify(analyticsService).logSearch(any(AnalyticsService.SearchEventBuilder.class));
        }
    }

    private TaxCalculationResponse createTaxResponse(Region region, TaxType taxType, BigDecimal amount) {
        TaxCalculationResponse response = new TaxCalculationResponse();
        response.setRegion(region);
        response.setTaxType(taxType);
        response.setAmount(amount);
        response.setIsExempt(false);
        return response;
    }

    private TaxCalculationResponse createExemptResponse(Region region, TaxType taxType, String reason) {
        TaxCalculationResponse response = new TaxCalculationResponse();
        response.setRegion(region);
        response.setTaxType(taxType);
        response.setAmount(BigDecimal.ZERO);
        response.setIsExempt(true);
        response.setExemptionReason(reason);
        return response;
    }
}
