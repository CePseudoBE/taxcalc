package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.entity.Brand;
import be.hoffmann.backtaxes.entity.Model;
import be.hoffmann.backtaxes.entity.Variant;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.exception.GlobalExceptionHandler;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.service.ModelService;
import be.hoffmann.backtaxes.service.VariantService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ModelControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ModelService modelService;

    @Mock
    private VariantService variantService;

    @BeforeEach
    void setUp() {
        ModelController controller = new ModelController(modelService, variantService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("GET /api/models")
    class SearchModelsTests {

        @Test
        @DisplayName("should return empty list when no search parameter")
        void shouldReturnEmptyWhenNoSearch() throws Exception {
            mockMvc.perform(get("/api/models"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));

            verify(modelService, never()).searchByName(anyString());
        }

        @Test
        @DisplayName("should return empty list for blank search")
        void shouldReturnEmptyForBlankSearch() throws Exception {
            mockMvc.perform(get("/api/models")
                            .param("search", "   "))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));

            verify(modelService, never()).searchByName(anyString());
        }
    }

    @Nested
    @DisplayName("GET /api/models/{id}")
    class GetModelByIdTests {

        @Test
        @DisplayName("should return 404 when model not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(modelService.findById(999L))
                    .thenThrow(new ResourceNotFoundException("Model", "id", 999L));

            mockMvc.perform(get("/api/models/999"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/models/{id}/variants")
    class GetVariantsByModelTests {

        @Test
        @DisplayName("should return variants for model")
        void shouldReturnVariantsForModel() throws Exception {
            Brand mercedes = createBrand(3L, "Mercedes");
            Model classA = createModel(5L, "Classe A", mercedes);

            Variant a180 = createVariant(1L, "A 180", classA, 100, 7, FuelType.petrol);
            Variant a200 = createVariant(2L, "A 200", classA, 120, 9, FuelType.petrol);

            when(variantService.findByModelId(5L)).thenReturn(Arrays.asList(a180, a200));

            mockMvc.perform(get("/api/models/5/variants"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].name", is("A 180")))
                    .andExpect(jsonPath("$.data[0].powerKw", is(100)))
                    .andExpect(jsonPath("$.data[1].name", is("A 200")));
        }

        @Test
        @DisplayName("should return empty list when model has no variants")
        void shouldReturnEmptyWhenNoVariants() throws Exception {
            when(variantService.findByModelId(99L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/models/99/variants"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

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
