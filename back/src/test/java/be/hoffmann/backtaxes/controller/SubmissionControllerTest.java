package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.SubmissionReviewRequest;
import be.hoffmann.backtaxes.dto.request.VehicleSubmissionRequest;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.entity.VehicleSubmission;
import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.SubmissionStatus;
import be.hoffmann.backtaxes.exception.GlobalExceptionHandler;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.service.SubmissionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class SubmissionControllerTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @Mock
    private SubmissionService submissionService;

    private User testUser;
    private User moderatorUser;
    private User otherUser;

    @BeforeEach
    void setUp() {
        SubmissionController controller = new SubmissionController(submissionService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        // Setup test users
        testUser = createUser(1L, "user@example.com", false);
        moderatorUser = createUser(2L, "moderator@example.com", true);
        otherUser = createUser(3L, "other@example.com", false);

        // Default: setup security context with test user
        setupSecurityContext(testUser);
    }

    private User createUser(Long id, String email, boolean isModerator) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setGoogleId("google-" + id);
        user.setIsModerator(isModerator);
        user.setIsAdmin(false);
        return user;
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

    private VehicleSubmission createSubmission(Long id, User submitter, SubmissionStatus status) {
        VehicleSubmission submission = new VehicleSubmission();
        submission.setId(id);
        submission.setSubmitter(submitter);
        submission.setStatus(status);
        submission.setBrandName("BMW");
        submission.setModelName("Serie 3");
        submission.setVariantName("320d");
        submission.setYearStart(2020);
        submission.setYearEnd(2023);
        submission.setPowerKw(140);
        submission.setFiscalHp(10);
        submission.setFuel(FuelType.diesel);
        submission.setEuroNorm(EuroNorm.euro_6);
        submission.setCo2Wltp(130);
        return submission;
    }

    private VehicleSubmissionRequest createValidRequest() {
        VehicleSubmissionRequest request = new VehicleSubmissionRequest();
        request.setBrandName("BMW");
        request.setModelName("Serie 3");
        request.setVariantName("320d xDrive");
        request.setYearStart(2020);
        request.setPowerKw(140);
        request.setFiscalHp(10);
        request.setFuel(FuelType.diesel);
        request.setEuroNorm(EuroNorm.euro_6);
        request.setCo2Wltp(130);
        return request;
    }

    // ==================== POST /api/submissions ====================

    @Nested
    @DisplayName("POST /api/submissions")
    class CreateSubmissionTests {

        @Test
        @DisplayName("should create submission for authenticated user")
        void shouldCreateSubmission() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.pending);

            when(submissionService.create(any(VehicleSubmissionRequest.class), eq(testUser)))
                    .thenReturn(submission);

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message", is("Submission created.")))
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.status", is("pending")))
                    .andExpect(jsonPath("$.data.vehicleData.brandName", is("BMW")))
                    .andExpect(jsonPath("$.data.vehicleData.modelName", is("Serie 3")))
                    .andExpect(jsonPath("$.data.vehicleData.variantName", is("320d")))
                    .andExpect(jsonPath("$.data.submitterId", is(1)));

            verify(submissionService).create(any(VehicleSubmissionRequest.class), eq(testUser));
        }

        @Test
        @DisplayName("should return 400 when brand name is missing")
        void shouldReturn400WhenBrandNameMissing() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            request.setBrandName(null);

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

            verify(submissionService, never()).create(any(), any());
        }

        @Test
        @DisplayName("should return 400 when fuel type is missing")
        void shouldReturn400WhenFuelTypeMissing() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            request.setFuel(null);

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

            verify(submissionService, never()).create(any(), any());
        }

        @Test
        @DisplayName("should return 400 when power is not positive")
        void shouldReturn400WhenPowerNotPositive() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            request.setPowerKw(-10);

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

            verify(submissionService, never()).create(any(), any());
        }

        @Test
        @DisplayName("should return 5xx when not authenticated")
        void shouldReturn5xxWhenNotAuthenticated() throws Exception {
            clearSecurityContext();
            VehicleSubmissionRequest request = createValidRequest();

            // Without Spring Security filter, controller gets null authentication and throws 500
            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());

            verify(submissionService, never()).create(any(), any());
        }
    }

    // ==================== GET /api/submissions/mine ====================

    @Nested
    @DisplayName("GET /api/submissions/mine")
    class GetMySubmissionsTests {

        @Test
        @DisplayName("should return user's submissions")
        void shouldReturnUsersSubmissions() throws Exception {
            List<VehicleSubmission> submissions = List.of(
                    createSubmission(1L, testUser, SubmissionStatus.pending),
                    createSubmission(2L, testUser, SubmissionStatus.approved)
            );

            when(submissionService.findByUser(1L)).thenReturn(submissions);

            mockMvc.perform(get("/api/submissions/mine"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id", is(1)))
                    .andExpect(jsonPath("$.data[0].status", is("pending")))
                    .andExpect(jsonPath("$.data[1].id", is(2)))
                    .andExpect(jsonPath("$.data[1].status", is("approved")));

            verify(submissionService).findByUser(1L);
        }

        @Test
        @DisplayName("should return empty list when user has no submissions")
        void shouldReturnEmptyList() throws Exception {
            when(submissionService.findByUser(1L)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/submissions/mine"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));

            verify(submissionService).findByUser(1L);
        }

        @Test
        @DisplayName("should return 5xx when not authenticated")
        void shouldReturn5xxWhenNotAuthenticated() throws Exception {
            clearSecurityContext();

            mockMvc.perform(get("/api/submissions/mine"))
                    .andExpect(status().is5xxServerError());

            verify(submissionService, never()).findByUser(anyLong());
        }
    }

    // ==================== GET /api/submissions/{id} ====================

    @Nested
    @DisplayName("GET /api/submissions/{id}")
    class GetSubmissionByIdTests {

        @Test
        @DisplayName("should return submission for owner")
        void shouldReturnSubmissionForOwner() throws Exception {
            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.pending);

            when(submissionService.findById(1L)).thenReturn(submission);

            mockMvc.perform(get("/api/submissions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)))
                    .andExpect(jsonPath("$.data.status", is("pending")))
                    .andExpect(jsonPath("$.data.vehicleData.brandName", is("BMW")));

            verify(submissionService).findById(1L);
        }

        @Test
        @DisplayName("should return submission for moderator")
        void shouldReturnSubmissionForModerator() throws Exception {
            setupSecurityContext(moderatorUser);
            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.pending);

            when(submissionService.findById(1L)).thenReturn(submission);

            mockMvc.perform(get("/api/submissions/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.id", is(1)));

            verify(submissionService).findById(1L);
        }

        @Test
        @DisplayName("should return 403 when accessing another user's submission")
        void shouldReturn403WhenAccessingOthersSubmission() throws Exception {
            VehicleSubmission submission = createSubmission(1L, otherUser, SubmissionStatus.pending);

            when(submissionService.findById(1L)).thenReturn(submission);

            mockMvc.perform(get("/api/submissions/1"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code", is("FORBIDDEN")));

            verify(submissionService).findById(1L);
        }

        @Test
        @DisplayName("should return 404 when submission not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(submissionService.findById(999L))
                    .thenThrow(new ResourceNotFoundException("VehicleSubmission", "id", 999L));

            mockMvc.perform(get("/api/submissions/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
        }

        @Test
        @DisplayName("should return 5xx when not authenticated")
        void shouldReturn5xxWhenNotAuthenticated() throws Exception {
            clearSecurityContext();

            mockMvc.perform(get("/api/submissions/1"))
                    .andExpect(status().is5xxServerError());

            verify(submissionService, never()).findById(anyLong());
        }
    }

    // ==================== GET /api/moderation/submissions ====================

    @Nested
    @DisplayName("GET /api/moderation/submissions")
    class GetSubmissionsByStatusTests {

        @Test
        @DisplayName("should return pending submissions without pagination")
        void shouldReturnPendingSubmissions() throws Exception {
            List<VehicleSubmission> submissions = List.of(
                    createSubmission(1L, testUser, SubmissionStatus.pending),
                    createSubmission(2L, otherUser, SubmissionStatus.pending)
            );

            when(submissionService.findByStatus(SubmissionStatus.pending)).thenReturn(submissions);

            mockMvc.perform(get("/api/moderation/submissions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(2)))
                    .andExpect(jsonPath("$.data[0].id", is(1)))
                    .andExpect(jsonPath("$.data[1].id", is(2)));

            verify(submissionService).findByStatus(SubmissionStatus.pending);
        }

        @Test
        @DisplayName("should return submissions by specific status")
        void shouldReturnSubmissionsByStatus() throws Exception {
            List<VehicleSubmission> submissions = List.of(
                    createSubmission(1L, testUser, SubmissionStatus.approved)
            );

            when(submissionService.findByStatus(SubmissionStatus.approved)).thenReturn(submissions);

            mockMvc.perform(get("/api/moderation/submissions")
                            .param("status", "approved"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].status", is("approved")));

            verify(submissionService).findByStatus(SubmissionStatus.approved);
        }

        @Test
        @DisplayName("should return rejected submissions")
        void shouldReturnRejectedSubmissions() throws Exception {
            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.rejected);
            submission.setFeedback("Duplicate vehicle");
            submission.setReviewer(moderatorUser);
            submission.setReviewedAt(Instant.now());

            when(submissionService.findByStatus(SubmissionStatus.rejected)).thenReturn(List.of(submission));

            mockMvc.perform(get("/api/moderation/submissions")
                            .param("status", "rejected"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].status", is("rejected")))
                    .andExpect(jsonPath("$.data[0].feedback", is("Duplicate vehicle")));

            verify(submissionService).findByStatus(SubmissionStatus.rejected);
        }

        @Test
        @DisplayName("should return paginated submissions when page params provided")
        void shouldReturnPaginatedSubmissions() throws Exception {
            List<VehicleSubmission> submissions = List.of(
                    createSubmission(1L, testUser, SubmissionStatus.pending)
            );
            PageImpl<VehicleSubmission> page = new PageImpl<>(
                    submissions,
                    PageRequest.of(0, 10),
                    1
            );

            when(submissionService.findByStatus(eq(SubmissionStatus.pending), any(Pageable.class)))
                    .thenReturn(page);

            mockMvc.perform(get("/api/moderation/submissions")
                            .param("page", "0")
                            .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content", hasSize(1)))
                    .andExpect(jsonPath("$.data.totalElements", is(1)))
                    .andExpect(jsonPath("$.data.totalPages", is(1)));

            verify(submissionService).findByStatus(eq(SubmissionStatus.pending), any(Pageable.class));
        }

        @Test
        @DisplayName("should return empty list when no submissions")
        void shouldReturnEmptyListWhenNoSubmissions() throws Exception {
            when(submissionService.findByStatus(SubmissionStatus.pending)).thenReturn(Collections.emptyList());

            mockMvc.perform(get("/api/moderation/submissions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data", hasSize(0)));
        }
    }

    // ==================== POST /api/moderation/submissions/{id}/review ====================

    @Nested
    @DisplayName("POST /api/moderation/submissions/{id}/review")
    class ReviewSubmissionTests {

        @BeforeEach
        void setUpModerator() {
            // Most review tests need a moderator
            setupSecurityContext(moderatorUser);
        }

        @Test
        @DisplayName("should approve submission")
        void shouldApproveSubmission() throws Exception {
            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.approved);
            submission.setReviewer(moderatorUser);
            submission.setReviewedAt(Instant.now());

            when(submissionService.approve(1L, moderatorUser)).thenReturn(submission);

            mockMvc.perform(post("/api/moderation/submissions/1/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "approved": true
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Submission approved.")))
                    .andExpect(jsonPath("$.data.status", is("approved")))
                    .andExpect(jsonPath("$.data.reviewedById", is(2)));

            verify(submissionService).approve(1L, moderatorUser);
            verify(submissionService, never()).reject(anyLong(), any(), anyString());
        }

        @Test
        @DisplayName("should reject submission with feedback")
        void shouldRejectSubmissionWithFeedback() throws Exception {
            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.rejected);
            submission.setReviewer(moderatorUser);
            submission.setReviewedAt(Instant.now());
            submission.setFeedback("Vehicle already exists in catalog");

            when(submissionService.reject(1L, moderatorUser, "Vehicle already exists in catalog"))
                    .thenReturn(submission);

            mockMvc.perform(post("/api/moderation/submissions/1/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "approved": false,
                                    "feedback": "Vehicle already exists in catalog"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Submission rejected.")))
                    .andExpect(jsonPath("$.data.status", is("rejected")))
                    .andExpect(jsonPath("$.data.feedback", is("Vehicle already exists in catalog")));

            verify(submissionService).reject(1L, moderatorUser, "Vehicle already exists in catalog");
            verify(submissionService, never()).approve(anyLong(), any());
        }

        @Test
        @DisplayName("should reject submission without feedback")
        void shouldRejectSubmissionWithoutFeedback() throws Exception {
            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.rejected);
            submission.setReviewer(moderatorUser);
            submission.setReviewedAt(Instant.now());

            when(submissionService.reject(1L, moderatorUser, null)).thenReturn(submission);

            mockMvc.perform(post("/api/moderation/submissions/1/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "approved": false
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", is("Submission rejected.")))
                    .andExpect(jsonPath("$.data.status", is("rejected")));

            verify(submissionService).reject(1L, moderatorUser, null);
        }

        @Test
        @DisplayName("should return 400 when approved field is missing")
        void shouldReturn400WhenApprovedMissing() throws Exception {
            mockMvc.perform(post("/api/moderation/submissions/1/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "feedback": "Some feedback"
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

            verify(submissionService, never()).approve(anyLong(), any());
            verify(submissionService, never()).reject(anyLong(), any(), anyString());
        }

        @Test
        @DisplayName("should return 400 when submission is not pending")
        void shouldReturn400WhenSubmissionNotPending() throws Exception {
            when(submissionService.approve(1L, moderatorUser))
                    .thenThrow(new ValidationException("Submission is not pending"));

            mockMvc.perform(post("/api/moderation/submissions/1/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "approved": true
                                }
                                """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));
        }

        @Test
        @DisplayName("should return 404 when submission not found")
        void shouldReturn404WhenNotFound() throws Exception {
            when(submissionService.approve(999L, moderatorUser))
                    .thenThrow(new ResourceNotFoundException("VehicleSubmission", "id", 999L));

            mockMvc.perform(post("/api/moderation/submissions/999/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "approved": true
                                }
                                """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error.code", is("NOT_FOUND")));
        }

        @Test
        @DisplayName("should return 5xx when not authenticated")
        void shouldReturn5xxWhenNotAuthenticated() throws Exception {
            clearSecurityContext();

            mockMvc.perform(post("/api/moderation/submissions/1/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "approved": true
                                }
                                """))
                    .andExpect(status().is5xxServerError());

            verify(submissionService, never()).approve(anyLong(), any());
            verify(submissionService, never()).reject(anyLong(), any(), anyString());
        }

        @Test
        @DisplayName("regular user can call review endpoint - access control handled elsewhere")
        void regularUserCanCallReviewEndpoint() throws Exception {
            // Note: In a real app, moderator-only access would be enforced by Spring Security
            // The controller does not check isModerator for the review endpoint
            // This test documents that behavior - access control is expected at the security layer
            setupSecurityContext(testUser);

            VehicleSubmission submission = createSubmission(1L, otherUser, SubmissionStatus.approved);
            submission.setReviewer(testUser);
            submission.setReviewedAt(Instant.now());

            when(submissionService.approve(1L, testUser)).thenReturn(submission);

            mockMvc.perform(post("/api/moderation/submissions/1/review")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "approved": true
                                }
                                """))
                    .andExpect(status().isOk());

            // The controller allows this - security should be handled at the filter level
            verify(submissionService).approve(1L, testUser);
        }
    }

    // ==================== Edge Cases and Integration Scenarios ====================

    @Nested
    @DisplayName("Edge Cases")
    class EdgeCaseTests {

        @Test
        @DisplayName("should handle submission with all optional fields")
        void shouldHandleSubmissionWithAllOptionalFields() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            request.setYearEnd(2023);
            request.setCo2Nedc(120);
            request.setDisplacementCc(2000);
            request.setMmaKg(1800);
            request.setHasParticleFilter(true);

            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.pending);
            submission.setYearEnd(2023);
            submission.setCo2Nedc(120);
            submission.setDisplacementCc(2000);
            submission.setMmaKg(1800);
            submission.setHasParticleFilter(true);

            when(submissionService.create(any(VehicleSubmissionRequest.class), eq(testUser)))
                    .thenReturn(submission);

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.vehicleData.yearEnd", is(2023)))
                    .andExpect(jsonPath("$.data.vehicleData.co2Nedc", is(120)))
                    .andExpect(jsonPath("$.data.vehicleData.displacementCc", is(2000)))
                    .andExpect(jsonPath("$.data.vehicleData.mmaKg", is(1800)))
                    .andExpect(jsonPath("$.data.vehicleData.hasParticleFilter", is(true)));
        }

        @Test
        @DisplayName("should handle submission for electric vehicle")
        void shouldHandleElectricVehicleSubmission() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            request.setBrandName("Tesla");
            request.setModelName("Model 3");
            request.setVariantName("Long Range");
            request.setFuel(FuelType.electric);
            request.setCo2Wltp(0);

            VehicleSubmission submission = new VehicleSubmission();
            submission.setId(1L);
            submission.setSubmitter(testUser);
            submission.setStatus(SubmissionStatus.pending);
            submission.setBrandName("Tesla");
            submission.setModelName("Model 3");
            submission.setVariantName("Long Range");
            submission.setFuel(FuelType.electric);
            submission.setCo2Wltp(0);
            submission.setYearStart(2023);
            submission.setPowerKw(324);
            submission.setFiscalHp(15);
            submission.setEuroNorm(EuroNorm.euro_6);

            when(submissionService.create(any(VehicleSubmissionRequest.class), eq(testUser)))
                    .thenReturn(submission);

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.vehicleData.brandName", is("Tesla")))
                    .andExpect(jsonPath("$.data.vehicleData.fuel", is("electric")))
                    .andExpect(jsonPath("$.data.vehicleData.co2Wltp", is(0)));
        }

        @Test
        @DisplayName("should handle hybrid vehicle submission")
        void shouldHandleHybridVehicleSubmission() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            request.setFuel(FuelType.hybrid_petrol);

            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.pending);
            submission.setFuel(FuelType.hybrid_petrol);

            when(submissionService.create(any(VehicleSubmissionRequest.class), eq(testUser)))
                    .thenReturn(submission);

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.vehicleData.fuel", is("hybrid_petrol")));
        }

        @Test
        @DisplayName("should handle brand name at max length")
        void shouldHandleBrandNameAtMaxLength() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            String maxLengthBrand = "A".repeat(100);
            request.setBrandName(maxLengthBrand);

            VehicleSubmission submission = createSubmission(1L, testUser, SubmissionStatus.pending);
            submission.setBrandName(maxLengthBrand);

            when(submissionService.create(any(VehicleSubmissionRequest.class), eq(testUser)))
                    .thenReturn(submission);

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.vehicleData.brandName", is(maxLengthBrand)));
        }

        @Test
        @DisplayName("should return 400 when brand name exceeds max length")
        void shouldReturn400WhenBrandNameTooLong() throws Exception {
            VehicleSubmissionRequest request = createValidRequest();
            request.setBrandName("A".repeat(101));

            mockMvc.perform(post("/api/submissions")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.error.code", is("VALIDATION_ERROR")));

            verify(submissionService, never()).create(any(), any());
        }
    }
}
