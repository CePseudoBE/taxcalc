package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.entity.Brand;
import be.hoffmann.backtaxes.entity.Model;
import be.hoffmann.backtaxes.entity.Variant;
import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.exception.GlobalExceptionHandler;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.service.VariantService;
import be.hoffmann.backtaxes.service.VariantService.VariantSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class VariantControllerTest {

    private MockMvc mockMvc;

    @Mock
    private VariantService variantService;

    @BeforeEach
    void setUp() {
        VariantController controller = new VariantController(variantService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/variants")
    class SearchVariantsTests {

        @Test
        @DisplayName("should return empty list when no filters provided")
        void shouldReturnEmptyWhenNoFilters() throws Exception {
            mockMvc.perform(get("/api/variants"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));

            verify(variantService, never()).search(any());
        }

        @Test
        @DisplayName("should search by brandId")
        void shouldSearchByBrandId() throws Exception {
            Brand bmw = createBrand(1L, "BMW");
            Model serie3 = createModel(1L, "Serie 3", bmw);
            Variant variant = createVariant(1L, "320d", serie3, 140, 10, FuelType.diesel);

            when(variantService.search(any())).thenReturn(List.of(variant));

            mockMvc.perform(get("/api/variants")
                            .param("brandId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].name", is("320d")));

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            assertEquals(1L, captor.getValue().getBrandId());
        }

        @Test
        @DisplayName("should search by modelId")
        void shouldSearchByModelId() throws Exception {
            Brand bmw = createBrand(1L, "BMW");
            Model serie3 = createModel(1L, "Serie 3", bmw);
            Variant variant = createVariant(1L, "320d", serie3, 140, 10, FuelType.diesel);

            when(variantService.search(any())).thenReturn(List.of(variant));

            mockMvc.perform(get("/api/variants")
                            .param("modelId", "1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            assertEquals(1L, captor.getValue().getModelId());
        }

        @Test
        @DisplayName("should search by fuel type")
        void shouldSearchByFuelType() throws Exception {
            Brand audi = createBrand(2L, "Audi");
            Model a4 = createModel(2L, "A4", audi);
            Variant variant = createVariant(2L, "A4 e-tron", a4, 150, 0, FuelType.electric);

            when(variantService.search(any())).thenReturn(List.of(variant));

            mockMvc.perform(get("/api/variants")
                            .param("fuel", "electric"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)));

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            assertNotNull(captor.getValue().getFuelTypes());
            assertTrue(captor.getValue().getFuelTypes().contains(FuelType.electric));
        }

        @Test
        @DisplayName("should search by multiple fuel types")
        void shouldSearchByMultipleFuelTypes() throws Exception {
            when(variantService.search(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/variants")
                            .param("fuel", "petrol", "diesel"))
                    .andExpect(status().isOk());

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            assertEquals(2, captor.getValue().getFuelTypes().size());
            assertTrue(captor.getValue().getFuelTypes().containsAll(
                    Arrays.asList(FuelType.petrol, FuelType.diesel)));
        }

        @Test
        @DisplayName("should search by euro norm")
        void shouldSearchByEuroNorm() throws Exception {
            when(variantService.search(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/variants")
                            .param("euroNorm", "euro_6"))
                    .andExpect(status().isOk());

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            assertTrue(captor.getValue().getEuroNorms().contains(EuroNorm.euro_6));
        }

        @Test
        @DisplayName("should search by power range")
        void shouldSearchByPowerRange() throws Exception {
            when(variantService.search(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/variants")
                            .param("minPower", "100")
                            .param("maxPower", "200"))
                    .andExpect(status().isOk());

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            assertEquals(100, captor.getValue().getMinPower());
            assertEquals(200, captor.getValue().getMaxPower());
        }

        @Test
        @DisplayName("should search by year range")
        void shouldSearchByYearRange() throws Exception {
            when(variantService.search(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/variants")
                            .param("minYear", "2020")
                            .param("maxYear", "2025"))
                    .andExpect(status().isOk());

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            assertEquals(2020, captor.getValue().getMinYear());
            assertEquals(2025, captor.getValue().getMaxYear());
        }

        @Test
        @DisplayName("should search by max CO2")
        void shouldSearchByMaxCo2() throws Exception {
            when(variantService.search(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/variants")
                            .param("maxCo2", "120"))
                    .andExpect(status().isOk());

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            assertEquals(120, captor.getValue().getMaxCo2());
        }

        @Test
        @DisplayName("should search with multiple filters")
        void shouldSearchWithMultipleFilters() throws Exception {
            when(variantService.search(any())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/variants")
                            .param("brandId", "1")
                            .param("fuel", "diesel")
                            .param("euroNorm", "euro_6")
                            .param("minPower", "100")
                            .param("maxCo2", "150"))
                    .andExpect(status().isOk());

            ArgumentCaptor<VariantSearchCriteria> captor = ArgumentCaptor.forClass(VariantSearchCriteria.class);
            verify(variantService).search(captor.capture());
            VariantSearchCriteria criteria = captor.getValue();
            assertEquals(1L, criteria.getBrandId());
            assertTrue(criteria.getFuelTypes().contains(FuelType.diesel));
            assertTrue(criteria.getEuroNorms().contains(EuroNorm.euro_6));
            assertEquals(100, criteria.getMinPower());
            assertEquals(150, criteria.getMaxCo2());
        }
    }

    @Nested
    @DisplayName("GET /api/variants/{id}")
    class GetVariantByIdTests {

        @Test
        @DisplayName("should return variant details")
        void shouldReturnVariantDetails() throws Exception {
            Brand bmw = createBrand(1L, "BMW");
            Model serie3 = createModel(1L, "Serie 3", bmw);
            Variant variant = createVariant(1L, "320d xDrive", serie3, 140, 10, FuelType.diesel);
            variant.setCo2Wltp(125);
            variant.setEuroNorm(EuroNorm.euro_6);
            variant.setDisplacementCc(1995);

            when(variantService.findByIdWithDetails(1L)).thenReturn(variant);

            mockMvc.perform(get("/api/variants/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.name", is("320d xDrive")))
                    .andExpect(jsonPath("$.data.powerKw", is(140)))
                    .andExpect(jsonPath("$.data.fiscalHp", is(10)))
                    .andExpect(jsonPath("$.data.fuel", is("diesel")))
                    .andExpect(jsonPath("$.data.co2Wltp", is(125)))
                    .andExpect(jsonPath("$.data.euroNorm", is("euro_6")))
                    .andExpect(jsonPath("$.data.displacementCc", is(1995)));
        }

        @Test
        @DisplayName("should return 404 when variant not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(variantService.findByIdWithDetails(999L))
                    .thenThrow(new ResourceNotFoundException("Variant", "id", 999L));

            mockMvc.perform(get("/api/variants/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
        }
    }

    // Helper methods
    private Brand createBrand(Long id, String name) {
        Brand brand = new Brand();
        brand.setId(id);
        brand.setName(name);
        return brand;
    }

    private Model createModel(Long id, String name, Brand brand) {
        Model model = new Model();
        model.setId(id);
        model.setName(name);
        model.setBrand(brand);
        return model;
    }

    private Variant createVariant(Long id, String name, Model model, int powerKw, int fiscalHp, FuelType fuel) {
        Variant variant = new Variant();
        variant.setId(id);
        variant.setName(name);
        variant.setModel(model);
        variant.setPowerKw(powerKw);
        variant.setFiscalHp(fiscalHp);
        variant.setFuel(fuel);
        return variant;
    }
}
