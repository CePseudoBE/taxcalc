package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.SavedSearchRequest;
import be.hoffmann.backtaxes.dto.response.SavedSearchResponse;
import be.hoffmann.backtaxes.entity.*;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.exception.GlobalExceptionHandler;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.service.SavedSearchService;
import be.hoffmann.backtaxes.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SavedSearchControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SavedSearchService savedSearchService;

    @Mock
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        SavedSearchController controller = new SavedSearchController(savedSearchService, userService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Setup test user
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPasswordHash("hashedpassword");
        testUser.setIsModerator(false);
        testUser.setIsAdmin(false);

        // Setup security context
        setupSecurityContext(testUser);
    }

    private void setupSecurityContext(User user) {
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, "token", Collections.emptyList());
        securityContext.setAuthentication(auth);
        SecurityContextHolder.setContext(securityContext);
    }

    private void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("POST /api/saved-searches")
    class SaveSearchTests {

        @Test
        @DisplayName("should save search with variant")
        void shouldSaveSearchWithVariant() throws Exception {
            SavedSearchRequest request = new SavedSearchRequest();
            request.setVariantId(1L);
            request.setRegion(Region.wallonia);
            request.setFirstRegistrationDate(LocalDate.of(2023, 6, 15));
            request.setLabel("Ma recherche BMW");

            SavedSearch savedSearch = createSavedSearchWithVariant();
            SavedSearchResponse response = createSavedSearchResponse();

            when(savedSearchService.save(any(), eq(testUser))).thenReturn(savedSearch);
            when(savedSearchService.toResponse(savedSearch)).thenReturn(response);

            mockMvc.perform(post("/api/saved-searches")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message", is("Search saved.")))
                    .andExpect(jsonPath("$.data.id", is(1)));

            verify(savedSearchService).save(any(), eq(testUser));
        }

        @Test
        @DisplayName("should return 5xx when no authentication context")
        void shouldFailWhenNoAuthenticationContext() throws Exception {
            clearSecurityContext();

            SavedSearchRequest request = new SavedSearchRequest();
            request.setVariantId(1L);
            request.setRegion(Region.wallonia);

            // Without Spring Security filter, controller will get null principal and throw 500 or 401
            mockMvc.perform(post("/api/saved-searches")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());

            verify(savedSearchService, never()).save(any(), any());
        }
    }

    @Nested
    @DisplayName("GET /api/saved-searches")
    class GetMySavedSearchesTests {

        @Test
        @DisplayName("should return user's saved searches")
        void shouldReturnUsersSavedSearches() throws Exception {
            List<SavedSearch> savedSearches = List.of(createSavedSearchWithVariant());
            List<SavedSearchResponse> responses = List.of(createSavedSearchResponse());

            when(savedSearchService.findByUser(1L)).thenReturn(savedSearches);
            when(savedSearchService.toResponseList(savedSearches)).thenReturn(responses);

            mockMvc.perform(get("/api/saved-searches"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].id", is(1)))
                    .andExpect(jsonPath("$.data[0].label", is("Ma recherche")));
        }

        @Test
        @DisplayName("should return empty list when no saved searches")
        void shouldReturnEmptyList() throws Exception {
            when(savedSearchService.findByUser(1L)).thenReturn(Collections.emptyList());
            when(savedSearchService.toResponseList(Collections.emptyList())).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/saved-searches"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }

        @Test
        @DisplayName("should fail when no authentication context")
        void shouldFailWhenNoAuthenticationContext() throws Exception {
            clearSecurityContext();

            // Without Spring Security filter, controller will get null principal and throw 500
            mockMvc.perform(get("/api/saved-searches"))
                    .andExpect(status().is5xxServerError());
        }
    }

    @Nested
    @DisplayName("GET /api/saved-searches/{id}")
    class GetSavedSearchByIdTests {

        @Test
        @DisplayName("should return saved search details")
        void shouldReturnSavedSearchDetails() throws Exception {
            SavedSearch savedSearch = createSavedSearchWithVariant();
            SavedSearchResponse response = createSavedSearchResponse();

            when(savedSearchService.findById(1L)).thenReturn(savedSearch);
            when(savedSearchService.toResponse(savedSearch)).thenReturn(response);

            mockMvc.perform(get("/api/saved-searches/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.label", is("Ma recherche")))
                    .andExpect(jsonPath("$.data.region", is("wallonia")));
        }

        @Test
        @DisplayName("should return 403 when accessing another user's search")
        void shouldReturn403WhenAccessingOthersSearch() throws Exception {
            User otherUser = new User();
            otherUser.setId(2L);

            SavedSearch savedSearch = createSavedSearchWithVariant();
            savedSearch.setUser(otherUser);

            when(savedSearchService.findById(1L)).thenReturn(savedSearch);

            mockMvc.perform(get("/api/saved-searches/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));
        }

        @Test
        @DisplayName("should return 404 when search not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(savedSearchService.findById(999L))
                    .thenThrow(new ResourceNotFoundException("SavedSearch", "id", 999L));

            mockMvc.perform(get("/api/saved-searches/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
        }
    }

    @Nested
    @DisplayName("DELETE /api/saved-searches/{id}")
    class DeleteSavedSearchTests {

        @Test
        @DisplayName("should delete saved search")
        void shouldDeleteSavedSearch() throws Exception {
            doNothing().when(savedSearchService).delete(1L, 1L);

            mockMvc.perform(delete("/api/saved-searches/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Search deleted.")));

            verify(savedSearchService).delete(1L, 1L);
        }

        @Test
        @DisplayName("should return 404 when search not found")
        void shouldReturn404WhenNotFound() throws Exception {
            doThrow(new ResourceNotFoundException("SavedSearch", "id", 999L))
                    .when(savedSearchService).delete(999L, 1L);

            mockMvc.perform(delete("/api/saved-searches/999"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("should fail when no authentication context")
        void shouldFailWhenNoAuthenticationContext() throws Exception {
            clearSecurityContext();

            // Without Spring Security filter, controller will get null principal and throw 500
            mockMvc.perform(delete("/api/saved-searches/1"))
                    .andExpect(status().is5xxServerError());

            verify(savedSearchService, never()).delete(anyLong(), anyLong());
        }
    }

    // Helper methods
    private SavedSearch createSavedSearchWithVariant() {
        Brand brand = new Brand();
        brand.setId(1L);
        brand.setName("BMW");

        Model model = new Model();
        model.setId(1L);
        model.setName("Serie 3");
        model.setBrand(brand);

        Variant variant = new Variant();
        variant.setId(1L);
        variant.setName("320d");
        variant.setModel(model);
        variant.setPowerKw(140);
        variant.setFuel(FuelType.diesel);

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setId(1L);
        savedSearch.setUser(testUser);
        savedSearch.setVariant(variant);
        savedSearch.setRegion(Region.wallonia);
        savedSearch.setFirstRegistrationDate(LocalDate.of(2023, 6, 15));
        savedSearch.setLabel("Ma recherche");
        // Note: createdAt is auto-generated by the database

        return savedSearch;
    }

    private SavedSearchResponse createSavedSearchResponse() {
        SavedSearchResponse.VehicleSummary vehicleSummary = new SavedSearchResponse.VehicleSummary(
                1L, "variant", "BMW", "Serie 3", "320d", 140, "diesel"
        );

        return new SavedSearchResponse(
                1L,
                "Ma recherche",
                Region.wallonia,
                LocalDate.of(2023, 6, 15),
                vehicleSummary,
                Instant.now()
        );
    }
}
