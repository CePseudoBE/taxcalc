package be.hoffmann.backtaxes.controller.admin;

import be.hoffmann.backtaxes.entity.AgeCoefficient;
import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.TaxExemption;
import be.hoffmann.backtaxes.entity.TaxParameter;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.exception.GlobalExceptionHandler;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class TaxConfigControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TaxBracketRepository taxBracketRepository;

    @Mock
    private TaxParameterRepository taxParameterRepository;

    @Mock
    private AgeCoefficientRepository ageCoefficientRepository;

    @Mock
    private TaxExemptionRepository taxExemptionRepository;

    @BeforeEach
    void setUp() {
        TaxConfigController controller = new TaxConfigController(
                taxBracketRepository,
                taxParameterRepository,
                ageCoefficientRepository,
                taxExemptionRepository
        );
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ==================== Tax Brackets Tests ====================

    @Nested
    @DisplayName("GET /api/admin/tax-brackets")
    class GetAllTaxBracketsTests {

        @Test
        @DisplayName("should return all tax brackets without pagination")
        void shouldReturnAllTaxBracketsWithoutPagination() throws Exception {
            TaxBracket bracket1 = createTaxBracket(1L, Region.wallonia, TaxType.tmc, "power_kw", 0, 70, new BigDecimal("61.50"));
            TaxBracket bracket2 = createTaxBracket(2L, Region.wallonia, TaxType.tmc, "power_kw", 71, 85, new BigDecimal("123.00"));

            when(taxBracketRepository.findAll()).thenReturn(List.of(bracket1, bracket2));

            mockMvc.perform(get("/api/admin/tax-brackets"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id", is(1)))
                    .andExpect(jsonPath("$.data[0].region", is("wallonia")))
                    .andExpect(jsonPath("$.data[0].taxType", is("tmc")))
                    .andExpect(jsonPath("$.data[0].bracketKey", is("power_kw")))
                    .andExpect(jsonPath("$.data[0].minValue", is(0)))
                    .andExpect(jsonPath("$.data[0].maxValue", is(70)))
                    .andExpect(jsonPath("$.data[0].amount", is(61.5)))
                    .andExpect(jsonPath("$.data[1].id", is(2)));

            verify(taxBracketRepository).findAll();
            verify(taxBracketRepository, never()).findByFilters(any(), any(), any());
        }

        @Test
        @DisplayName("should return tax brackets filtered by region")
        void shouldReturnTaxBracketsFilteredByRegion() throws Exception {
            TaxBracket bracket = createTaxBracket(1L, Region.flanders, TaxType.tmc, "co2", 0, 100, new BigDecimal("500.00"));
            Page<TaxBracket> page = new PageImpl<>(List.of(bracket));

            when(taxBracketRepository.findByFilters(eq(Region.flanders), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/tax-brackets")
                            .param("region", "flanders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].region", is("flanders")));

            verify(taxBracketRepository).findByFilters(eq(Region.flanders), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("should return tax brackets filtered by tax type")
        void shouldReturnTaxBracketsFilteredByTaxType() throws Exception {
            TaxBracket bracket = createTaxBracket(1L, Region.brussels, TaxType.annual, "fiscal_hp", 0, 8, new BigDecimal("100.00"));
            Page<TaxBracket> page = new PageImpl<>(List.of(bracket));

            when(taxBracketRepository.findByFilters(isNull(), eq(TaxType.annual), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/tax-brackets")
                            .param("taxType", "annual"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].taxType", is("annual")));

            verify(taxBracketRepository).findByFilters(isNull(), eq(TaxType.annual), any(Pageable.class));
        }

        @Test
        @DisplayName("should return paginated tax brackets")
        void shouldReturnPaginatedTaxBrackets() throws Exception {
            TaxBracket bracket = createTaxBracket(1L, Region.wallonia, TaxType.tmc, "power_kw", 0, 70, new BigDecimal("61.50"));
            Page<TaxBracket> page = new PageImpl<>(List.of(bracket), Pageable.ofSize(10), 50);

            when(taxBracketRepository.findByFilters(isNull(), isNull(), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/tax-brackets")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.page", is(0)))
                    .andExpect(jsonPath("$.data.size", is(10)))
                    .andExpect(jsonPath("$.data.totalElements", is(50)))
                    .andExpect(jsonPath("$.data.totalPages", is(5)));

            verify(taxBracketRepository).findByFilters(isNull(), isNull(), any(Pageable.class));
        }

        @Test
        @DisplayName("should return paginated tax brackets with filters")
        void shouldReturnPaginatedTaxBracketsWithFilters() throws Exception {
            TaxBracket bracket = createTaxBracket(1L, Region.wallonia, TaxType.tmc, "power_kw", 0, 70, new BigDecimal("61.50"));
            Page<TaxBracket> page = new PageImpl<>(List.of(bracket));

            when(taxBracketRepository.findByFilters(eq(Region.wallonia), eq(TaxType.tmc), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/admin/tax-brackets")
                            .param("region", "wallonia")
                            .param("taxType", "tmc")
                            .param("page", "0")
                            .param("size", "20"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.content[0].region", is("wallonia")))
                    .andExpect(jsonPath("$.data.content[0].taxType", is("tmc")));

            verify(taxBracketRepository).findByFilters(eq(Region.wallonia), eq(TaxType.tmc), any(Pageable.class));
        }
    }

    @Nested
    @DisplayName("GET /api/admin/tax-brackets/{id}")
    class GetTaxBracketByIdTests {

        @Test
        @DisplayName("should return tax bracket by id")
        void shouldReturnTaxBracketById() throws Exception {
            TaxBracket bracket = createTaxBracket(1L, Region.wallonia, TaxType.tmc, "power_kw", 0, 70, new BigDecimal("61.50"));

            when(taxBracketRepository.findById(1L)).thenReturn(Optional.of(bracket));

            mockMvc.perform(get("/api/admin/tax-brackets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.region", is("wallonia")))
                    .andExpect(jsonPath("$.data.taxType", is("tmc")))
                    .andExpect(jsonPath("$.data.bracketKey", is("power_kw")))
                    .andExpect(jsonPath("$.data.minValue", is(0)))
                    .andExpect(jsonPath("$.data.maxValue", is(70)))
                    .andExpect(jsonPath("$.data.amount", is(61.5)));

            verify(taxBracketRepository).findById(1L);
        }

        @Test
        @DisplayName("should return 404 when tax bracket not found")
        void shouldReturn404WhenTaxBracketNotFound() throws Exception {
            when(taxBracketRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/admin/tax-brackets/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("TaxBracket")))
                    .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));

            verify(taxBracketRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/admin/tax-brackets")
    class CreateTaxBracketTests {

        @Test
        @DisplayName("should create a new tax bracket")
        void shouldCreateNewTaxBracket() throws Exception {
            TaxBracket savedBracket = createTaxBracket(1L, Region.wallonia, TaxType.tmc, "power_kw", 0, 70, new BigDecimal("61.50"));

            when(taxBracketRepository.save(any(TaxBracket.class))).thenReturn(savedBracket);

            mockMvc.perform(post("/api/admin/tax-brackets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "bracketKey": "power_kw",
                                    "minValue": 0,
                                    "maxValue": 70,
                                    "amount": 61.50,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.region", is("wallonia")))
                    .andExpect(jsonPath("$.data.bracketKey", is("power_kw")))
                    .andExpect(jsonPath("$.message", containsString("created")));

            verify(taxBracketRepository).save(any(TaxBracket.class));
        }

        @Test
        @DisplayName("should ignore id in request body when creating")
        void shouldIgnoreIdInRequestBody() throws Exception {
            TaxBracket savedBracket = createTaxBracket(5L, Region.flanders, TaxType.tmc, "co2", 0, 100, new BigDecimal("500.00"));

            when(taxBracketRepository.save(any(TaxBracket.class))).thenAnswer(invocation -> {
                TaxBracket arg = invocation.getArgument(0);
                // Verify that ID was set to null before saving
                assert arg.getId() == null : "ID should be null when creating";
                arg.setId(5L);
                return arg;
            });

            mockMvc.perform(post("/api/admin/tax-brackets")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "id": 999,
                                    "region": "flanders",
                                    "taxType": "tmc",
                                    "bracketKey": "co2",
                                    "minValue": 0,
                                    "maxValue": 100,
                                    "amount": 500.00,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id", is(5)));

            verify(taxBracketRepository).save(any(TaxBracket.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/tax-brackets/{id}")
    class UpdateTaxBracketTests {

        @Test
        @DisplayName("should update existing tax bracket")
        void shouldUpdateExistingTaxBracket() throws Exception {
            TaxBracket updatedBracket = createTaxBracket(1L, Region.wallonia, TaxType.tmc, "power_kw", 0, 75, new BigDecimal("65.00"));

            when(taxBracketRepository.existsById(1L)).thenReturn(true);
            when(taxBracketRepository.save(any(TaxBracket.class))).thenReturn(updatedBracket);

            mockMvc.perform(put("/api/admin/tax-brackets/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "bracketKey": "power_kw",
                                    "minValue": 0,
                                    "maxValue": 75,
                                    "amount": 65.00,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.maxValue", is(75)))
                    .andExpect(jsonPath("$.data.amount", is(65.0)))
                    .andExpect(jsonPath("$.message", containsString("updated")));

            verify(taxBracketRepository).existsById(1L);
            verify(taxBracketRepository).save(any(TaxBracket.class));
        }

        @Test
        @DisplayName("should return 404 when updating non-existent tax bracket")
        void shouldReturn404WhenUpdatingNonExistentTaxBracket() throws Exception {
            when(taxBracketRepository.existsById(999L)).thenReturn(false);

            mockMvc.perform(put("/api/admin/tax-brackets/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "bracketKey": "power_kw",
                                    "minValue": 0,
                                    "maxValue": 70,
                                    "amount": 61.50,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));

            verify(taxBracketRepository).existsById(999L);
            verify(taxBracketRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/tax-brackets/{id}")
    class DeleteTaxBracketTests {

        @Test
        @DisplayName("should delete existing tax bracket")
        void shouldDeleteExistingTaxBracket() throws Exception {
            when(taxBracketRepository.existsById(1L)).thenReturn(true);
            doNothing().when(taxBracketRepository).deleteById(1L);

            mockMvc.perform(delete("/api/admin/tax-brackets/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", containsString("deleted")));

            verify(taxBracketRepository).existsById(1L);
            verify(taxBracketRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent tax bracket")
        void shouldReturn404WhenDeletingNonExistentTaxBracket() throws Exception {
            when(taxBracketRepository.existsById(999L)).thenReturn(false);

            mockMvc.perform(delete("/api/admin/tax-brackets/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));

            verify(taxBracketRepository).existsById(999L);
            verify(taxBracketRepository, never()).deleteById(any());
        }
    }

    // ==================== Tax Parameters Tests ====================

    @Nested
    @DisplayName("GET /api/admin/tax-parameters")
    class GetAllTaxParametersTests {

        @Test
        @DisplayName("should return all tax parameters")
        void shouldReturnAllTaxParameters() throws Exception {
            TaxParameter param1 = createTaxParameter(1L, Region.wallonia, TaxType.tmc, "co2_reference_wltp", new BigDecimal("136.0000"));
            TaxParameter param2 = createTaxParameter(2L, Region.flanders, TaxType.tmc, "min_amount", new BigDecimal("50.0000"));

            when(taxParameterRepository.findAll()).thenReturn(List.of(param1, param2));

            mockMvc.perform(get("/api/admin/tax-parameters"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id", is(1)))
                    .andExpect(jsonPath("$.data[0].region", is("wallonia")))
                    .andExpect(jsonPath("$.data[0].paramKey", is("co2_reference_wltp")))
                    .andExpect(jsonPath("$.data[1].id", is(2)));

            verify(taxParameterRepository).findAll();
        }

        @Test
        @DisplayName("should return tax parameters filtered by region and tax type")
        void shouldReturnTaxParametersFilteredByRegionAndTaxType() throws Exception {
            TaxParameter param = createTaxParameter(1L, Region.wallonia, TaxType.tmc, "co2_reference_wltp", new BigDecimal("136.0000"));

            when(taxParameterRepository.findByRegionAndTaxType(Region.wallonia, TaxType.tmc))
                    .thenReturn(List.of(param));

            mockMvc.perform(get("/api/admin/tax-parameters")
                            .param("region", "wallonia")
                            .param("taxType", "tmc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].region", is("wallonia")))
                    .andExpect(jsonPath("$.data[0].taxType", is("tmc")));

            verify(taxParameterRepository).findByRegionAndTaxType(Region.wallonia, TaxType.tmc);
        }

        @Test
        @DisplayName("should return all tax parameters when only one filter provided")
        void shouldReturnAllTaxParametersWhenOnlyOneFilterProvided() throws Exception {
            TaxParameter param = createTaxParameter(1L, Region.wallonia, TaxType.tmc, "co2_reference_wltp", new BigDecimal("136.0000"));

            when(taxParameterRepository.findAll()).thenReturn(List.of(param));

            // Only region provided, not taxType - should return all
            mockMvc.perform(get("/api/admin/tax-parameters")
                            .param("region", "wallonia"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            verify(taxParameterRepository).findAll();
        }
    }

    @Nested
    @DisplayName("GET /api/admin/tax-parameters/{id}")
    class GetTaxParameterByIdTests {

        @Test
        @DisplayName("should return tax parameter by id")
        void shouldReturnTaxParameterById() throws Exception {
            TaxParameter param = createTaxParameter(1L, Region.flanders, TaxType.tmc, "min_amount", new BigDecimal("50.0000"));

            when(taxParameterRepository.findById(1L)).thenReturn(Optional.of(param));

            mockMvc.perform(get("/api/admin/tax-parameters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.region", is("flanders")))
                    .andExpect(jsonPath("$.data.paramKey", is("min_amount")));

            verify(taxParameterRepository).findById(1L);
        }

        @Test
        @DisplayName("should return 404 when tax parameter not found")
        void shouldReturn404WhenTaxParameterNotFound() throws Exception {
            when(taxParameterRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/admin/tax-parameters/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("TaxParameter")));

            verify(taxParameterRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/admin/tax-parameters")
    class CreateTaxParameterTests {

        @Test
        @DisplayName("should create a new tax parameter")
        void shouldCreateNewTaxParameter() throws Exception {
            TaxParameter savedParam = createTaxParameter(1L, Region.brussels, TaxType.annual, "lpg_reduction", new BigDecimal("298.0000"));

            when(taxParameterRepository.save(any(TaxParameter.class))).thenReturn(savedParam);

            mockMvc.perform(post("/api/admin/tax-parameters")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "brussels",
                                    "taxType": "annual",
                                    "paramKey": "lpg_reduction",
                                    "paramValue": 298.0000,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.paramKey", is("lpg_reduction")))
                    .andExpect(jsonPath("$.message", containsString("created")));

            verify(taxParameterRepository).save(any(TaxParameter.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/tax-parameters/{id}")
    class UpdateTaxParameterTests {

        @Test
        @DisplayName("should update existing tax parameter")
        void shouldUpdateExistingTaxParameter() throws Exception {
            TaxParameter updatedParam = createTaxParameter(1L, Region.wallonia, TaxType.tmc, "co2_reference_wltp", new BigDecimal("140.0000"));

            when(taxParameterRepository.existsById(1L)).thenReturn(true);
            when(taxParameterRepository.save(any(TaxParameter.class))).thenReturn(updatedParam);

            mockMvc.perform(put("/api/admin/tax-parameters/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "paramKey": "co2_reference_wltp",
                                    "paramValue": 140.0000,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.message", containsString("updated")));

            verify(taxParameterRepository).existsById(1L);
            verify(taxParameterRepository).save(any(TaxParameter.class));
        }

        @Test
        @DisplayName("should return 404 when updating non-existent tax parameter")
        void shouldReturn404WhenUpdatingNonExistentTaxParameter() throws Exception {
            when(taxParameterRepository.existsById(999L)).thenReturn(false);

            mockMvc.perform(put("/api/admin/tax-parameters/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "paramKey": "co2_reference_wltp",
                                    "paramValue": 140.0000,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isNotFound());

            verify(taxParameterRepository).existsById(999L);
            verify(taxParameterRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/tax-parameters/{id}")
    class DeleteTaxParameterTests {

        @Test
        @DisplayName("should delete existing tax parameter")
        void shouldDeleteExistingTaxParameter() throws Exception {
            when(taxParameterRepository.existsById(1L)).thenReturn(true);
            doNothing().when(taxParameterRepository).deleteById(1L);

            mockMvc.perform(delete("/api/admin/tax-parameters/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", containsString("deleted")));

            verify(taxParameterRepository).existsById(1L);
            verify(taxParameterRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent tax parameter")
        void shouldReturn404WhenDeletingNonExistentTaxParameter() throws Exception {
            when(taxParameterRepository.existsById(999L)).thenReturn(false);

            mockMvc.perform(delete("/api/admin/tax-parameters/999"))
                    .andExpect(status().isNotFound());

            verify(taxParameterRepository).existsById(999L);
            verify(taxParameterRepository, never()).deleteById(any());
        }
    }

    // ==================== Age Coefficients Tests ====================

    @Nested
    @DisplayName("GET /api/admin/age-coefficients")
    class GetAllAgeCoefficientsTests {

        @Test
        @DisplayName("should return all age coefficients")
        void shouldReturnAllAgeCoefficients() throws Exception {
            AgeCoefficient coef1 = createAgeCoefficient(1L, Region.wallonia, TaxType.tmc, 0, new BigDecimal("1.0000"));
            AgeCoefficient coef2 = createAgeCoefficient(2L, Region.wallonia, TaxType.tmc, 1, new BigDecimal("0.9000"));

            when(ageCoefficientRepository.findAll()).thenReturn(List.of(coef1, coef2));

            mockMvc.perform(get("/api/admin/age-coefficients"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id", is(1)))
                    .andExpect(jsonPath("$.data[0].vehicleAgeYears", is(0)))
                    .andExpect(jsonPath("$.data[0].coefficient", is(1.0)))
                    .andExpect(jsonPath("$.data[1].vehicleAgeYears", is(1)))
                    .andExpect(jsonPath("$.data[1].coefficient", is(0.9)));

            verify(ageCoefficientRepository).findAll();
        }

        @Test
        @DisplayName("should return age coefficients filtered by region and tax type")
        void shouldReturnAgeCoefficientsFilteredByRegionAndTaxType() throws Exception {
            AgeCoefficient coef = createAgeCoefficient(1L, Region.flanders, TaxType.tmc, 5, new BigDecimal("0.5000"));

            when(ageCoefficientRepository.findByRegionAndTaxType(Region.flanders, TaxType.tmc))
                    .thenReturn(List.of(coef));

            mockMvc.perform(get("/api/admin/age-coefficients")
                            .param("region", "flanders")
                            .param("taxType", "tmc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].region", is("flanders")))
                    .andExpect(jsonPath("$.data[0].vehicleAgeYears", is(5)));

            verify(ageCoefficientRepository).findByRegionAndTaxType(Region.flanders, TaxType.tmc);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/age-coefficients/{id}")
    class GetAgeCoefficientByIdTests {

        @Test
        @DisplayName("should return age coefficient by id")
        void shouldReturnAgeCoefficientById() throws Exception {
            AgeCoefficient coef = createAgeCoefficient(1L, Region.brussels, TaxType.tmc, 10, new BigDecimal("0.2000"));

            when(ageCoefficientRepository.findById(1L)).thenReturn(Optional.of(coef));

            mockMvc.perform(get("/api/admin/age-coefficients/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.region", is("brussels")))
                    .andExpect(jsonPath("$.data.vehicleAgeYears", is(10)))
                    .andExpect(jsonPath("$.data.coefficient", is(0.2)));

            verify(ageCoefficientRepository).findById(1L);
        }

        @Test
        @DisplayName("should return 404 when age coefficient not found")
        void shouldReturn404WhenAgeCoefficientNotFound() throws Exception {
            when(ageCoefficientRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/admin/age-coefficients/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("AgeCoefficient")));

            verify(ageCoefficientRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/admin/age-coefficients")
    class CreateAgeCoefficientTests {

        @Test
        @DisplayName("should create a new age coefficient")
        void shouldCreateNewAgeCoefficient() throws Exception {
            AgeCoefficient savedCoef = createAgeCoefficient(1L, Region.wallonia, TaxType.tmc, 15, new BigDecimal("0.0000"));

            when(ageCoefficientRepository.save(any(AgeCoefficient.class))).thenReturn(savedCoef);

            mockMvc.perform(post("/api/admin/age-coefficients")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "vehicleAgeYears": 15,
                                    "coefficient": 0.0000,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.vehicleAgeYears", is(15)))
                    .andExpect(jsonPath("$.data.coefficient", is(0.0)))
                    .andExpect(jsonPath("$.message", containsString("created")));

            verify(ageCoefficientRepository).save(any(AgeCoefficient.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/age-coefficients/{id}")
    class UpdateAgeCoefficientTests {

        @Test
        @DisplayName("should update existing age coefficient")
        void shouldUpdateExistingAgeCoefficient() throws Exception {
            AgeCoefficient updatedCoef = createAgeCoefficient(1L, Region.wallonia, TaxType.tmc, 5, new BigDecimal("0.4500"));

            when(ageCoefficientRepository.existsById(1L)).thenReturn(true);
            when(ageCoefficientRepository.save(any(AgeCoefficient.class))).thenReturn(updatedCoef);

            mockMvc.perform(put("/api/admin/age-coefficients/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "vehicleAgeYears": 5,
                                    "coefficient": 0.4500,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.coefficient", is(0.45)))
                    .andExpect(jsonPath("$.message", containsString("updated")));

            verify(ageCoefficientRepository).existsById(1L);
            verify(ageCoefficientRepository).save(any(AgeCoefficient.class));
        }

        @Test
        @DisplayName("should return 404 when updating non-existent age coefficient")
        void shouldReturn404WhenUpdatingNonExistentAgeCoefficient() throws Exception {
            when(ageCoefficientRepository.existsById(999L)).thenReturn(false);

            mockMvc.perform(put("/api/admin/age-coefficients/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "vehicleAgeYears": 5,
                                    "coefficient": 0.4500,
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isNotFound());

            verify(ageCoefficientRepository).existsById(999L);
            verify(ageCoefficientRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/age-coefficients/{id}")
    class DeleteAgeCoefficientTests {

        @Test
        @DisplayName("should delete existing age coefficient")
        void shouldDeleteExistingAgeCoefficient() throws Exception {
            when(ageCoefficientRepository.existsById(1L)).thenReturn(true);
            doNothing().when(ageCoefficientRepository).deleteById(1L);

            mockMvc.perform(delete("/api/admin/age-coefficients/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", containsString("deleted")));

            verify(ageCoefficientRepository).existsById(1L);
            verify(ageCoefficientRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent age coefficient")
        void shouldReturn404WhenDeletingNonExistentAgeCoefficient() throws Exception {
            when(ageCoefficientRepository.existsById(999L)).thenReturn(false);

            mockMvc.perform(delete("/api/admin/age-coefficients/999"))
                    .andExpect(status().isNotFound());

            verify(ageCoefficientRepository).existsById(999L);
            verify(ageCoefficientRepository, never()).deleteById(any());
        }
    }

    // ==================== Tax Exemptions Tests ====================

    @Nested
    @DisplayName("GET /api/admin/tax-exemptions")
    class GetAllTaxExemptionsTests {

        @Test
        @DisplayName("should return all tax exemptions")
        void shouldReturnAllTaxExemptions() throws Exception {
            TaxExemption exemption1 = createTaxExemption(1L, Region.wallonia, TaxType.tmc, "fuel_electric");
            TaxExemption exemption2 = createTaxExemption(2L, Region.flanders, TaxType.annual, "fuel_hydrogen");

            when(taxExemptionRepository.findAll()).thenReturn(List.of(exemption1, exemption2));

            mockMvc.perform(get("/api/admin/tax-exemptions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id", is(1)))
                    .andExpect(jsonPath("$.data[0].region", is("wallonia")))
                    .andExpect(jsonPath("$.data[0].conditionKey", is("fuel_electric")))
                    .andExpect(jsonPath("$.data[1].conditionKey", is("fuel_hydrogen")));

            verify(taxExemptionRepository).findAll();
        }

        @Test
        @DisplayName("should return tax exemptions filtered by region and tax type")
        void shouldReturnTaxExemptionsFilteredByRegionAndTaxType() throws Exception {
            TaxExemption exemption = createTaxExemption(1L, Region.brussels, TaxType.tmc, "fuel_electric");

            when(taxExemptionRepository.findByRegionAndTaxType(Region.brussels, TaxType.tmc))
                    .thenReturn(List.of(exemption));

            mockMvc.perform(get("/api/admin/tax-exemptions")
                            .param("region", "brussels")
                            .param("taxType", "tmc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].region", is("brussels")));

            verify(taxExemptionRepository).findByRegionAndTaxType(Region.brussels, TaxType.tmc);
        }
    }

    @Nested
    @DisplayName("GET /api/admin/tax-exemptions/{id}")
    class GetTaxExemptionByIdTests {

        @Test
        @DisplayName("should return tax exemption by id")
        void shouldReturnTaxExemptionById() throws Exception {
            TaxExemption exemption = createTaxExemption(1L, Region.flanders, TaxType.tmc, "fuel_electric");

            when(taxExemptionRepository.findById(1L)).thenReturn(Optional.of(exemption));

            mockMvc.perform(get("/api/admin/tax-exemptions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.region", is("flanders")))
                    .andExpect(jsonPath("$.data.conditionKey", is("fuel_electric")));

            verify(taxExemptionRepository).findById(1L);
        }

        @Test
        @DisplayName("should return 404 when tax exemption not found")
        void shouldReturn404WhenTaxExemptionNotFound() throws Exception {
            when(taxExemptionRepository.findById(999L)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/admin/tax-exemptions/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message", containsString("TaxExemption")));

            verify(taxExemptionRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("POST /api/admin/tax-exemptions")
    class CreateTaxExemptionTests {

        @Test
        @DisplayName("should create a new tax exemption")
        void shouldCreateNewTaxExemption() throws Exception {
            TaxExemption savedExemption = createTaxExemption(1L, Region.wallonia, TaxType.annual, "fuel_hydrogen");

            when(taxExemptionRepository.save(any(TaxExemption.class))).thenReturn(savedExemption);

            mockMvc.perform(post("/api/admin/tax-exemptions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "annual",
                                    "conditionKey": "fuel_hydrogen",
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.conditionKey", is("fuel_hydrogen")))
                    .andExpect(jsonPath("$.message", containsString("created")));

            verify(taxExemptionRepository).save(any(TaxExemption.class));
        }
    }

    @Nested
    @DisplayName("PUT /api/admin/tax-exemptions/{id}")
    class UpdateTaxExemptionTests {

        @Test
        @DisplayName("should update existing tax exemption")
        void shouldUpdateExistingTaxExemption() throws Exception {
            TaxExemption updatedExemption = createTaxExemption(1L, Region.brussels, TaxType.tmc, "fuel_electric");
            updatedExemption.setValidTo(LocalDate.of(2030, 12, 31));

            when(taxExemptionRepository.existsById(1L)).thenReturn(true);
            when(taxExemptionRepository.save(any(TaxExemption.class))).thenReturn(updatedExemption);

            mockMvc.perform(put("/api/admin/tax-exemptions/1")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "brussels",
                                    "taxType": "tmc",
                                    "conditionKey": "fuel_electric",
                                    "validFrom": "2025-01-01",
                                    "validTo": "2030-12-31"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.message", containsString("updated")));

            verify(taxExemptionRepository).existsById(1L);
            verify(taxExemptionRepository).save(any(TaxExemption.class));
        }

        @Test
        @DisplayName("should return 404 when updating non-existent tax exemption")
        void shouldReturn404WhenUpdatingNonExistentTaxExemption() throws Exception {
            when(taxExemptionRepository.existsById(999L)).thenReturn(false);

            mockMvc.perform(put("/api/admin/tax-exemptions/999")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "region": "wallonia",
                                    "taxType": "tmc",
                                    "conditionKey": "fuel_electric",
                                    "validFrom": "2025-01-01"
                                }
                                """))
                    .andExpect(status().isNotFound());

            verify(taxExemptionRepository).existsById(999L);
            verify(taxExemptionRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("DELETE /api/admin/tax-exemptions/{id}")
    class DeleteTaxExemptionTests {

        @Test
        @DisplayName("should delete existing tax exemption")
        void shouldDeleteExistingTaxExemption() throws Exception {
            when(taxExemptionRepository.existsById(1L)).thenReturn(true);
            doNothing().when(taxExemptionRepository).deleteById(1L);

            mockMvc.perform(delete("/api/admin/tax-exemptions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", containsString("deleted")));

            verify(taxExemptionRepository).existsById(1L);
            verify(taxExemptionRepository).deleteById(1L);
        }

        @Test
        @DisplayName("should return 404 when deleting non-existent tax exemption")
        void shouldReturn404WhenDeletingNonExistentTaxExemption() throws Exception {
            when(taxExemptionRepository.existsById(999L)).thenReturn(false);

            mockMvc.perform(delete("/api/admin/tax-exemptions/999"))
                    .andExpect(status().isNotFound());

            verify(taxExemptionRepository).existsById(999L);
            verify(taxExemptionRepository, never()).deleteById(any());
        }
    }

    // ==================== Helper Methods ====================

    private TaxBracket createTaxBracket(Long id, Region region, TaxType taxType, String bracketKey,
                                         Integer minValue, Integer maxValue, BigDecimal amount) {
        TaxBracket bracket = new TaxBracket();
        bracket.setId(id);
        bracket.setRegion(region);
        bracket.setTaxType(taxType);
        bracket.setBracketKey(bracketKey);
        bracket.setMinValue(minValue);
        bracket.setMaxValue(maxValue);
        bracket.setAmount(amount);
        bracket.setValidFrom(LocalDate.of(2025, 1, 1));
        return bracket;
    }

    private TaxParameter createTaxParameter(Long id, Region region, TaxType taxType,
                                             String paramKey, BigDecimal paramValue) {
        TaxParameter param = new TaxParameter();
        param.setId(id);
        param.setRegion(region);
        param.setTaxType(taxType);
        param.setParamKey(paramKey);
        param.setParamValue(paramValue);
        param.setValidFrom(LocalDate.of(2025, 1, 1));
        return param;
    }

    private AgeCoefficient createAgeCoefficient(Long id, Region region, TaxType taxType,
                                                 Integer vehicleAgeYears, BigDecimal coefficient) {
        AgeCoefficient coef = new AgeCoefficient();
        coef.setId(id);
        coef.setRegion(region);
        coef.setTaxType(taxType);
        coef.setVehicleAgeYears(vehicleAgeYears);
        coef.setCoefficient(coefficient);
        coef.setValidFrom(LocalDate.of(2025, 1, 1));
        return coef;
    }

    private TaxExemption createTaxExemption(Long id, Region region, TaxType taxType, String conditionKey) {
        TaxExemption exemption = new TaxExemption();
        exemption.setId(id);
        exemption.setRegion(region);
        exemption.setTaxType(taxType);
        exemption.setConditionKey(conditionKey);
        exemption.setValidFrom(LocalDate.of(2025, 1, 1));
        return exemption;
    }
}
