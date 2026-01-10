package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.mapper.SubmissionMapper;
import be.hoffmann.backtaxes.dto.request.SubmissionReviewRequest;
import be.hoffmann.backtaxes.dto.request.VehicleSubmissionRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.PagedResponse;
import be.hoffmann.backtaxes.dto.response.SubmissionResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.entity.VehicleSubmission;
import be.hoffmann.backtaxes.entity.enums.SubmissionStatus;
import be.hoffmann.backtaxes.service.SubmissionService;
import be.hoffmann.backtaxes.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Soumissions", description = "Soumission et moderation de nouveaux vehicules")
@RestController
@RequestMapping("/api")
public class SubmissionController {

    private static final Logger audit = LoggerFactory.getLogger("audit");

    private final SubmissionService submissionService;

    public SubmissionController(SubmissionService submissionService) {
        this.submissionService = submissionService;
    }

    // ==================== Endpoints Utilisateur ====================

    @Operation(summary = "Soumettre un vehicule", description = "Soumet un nouveau vehicule pour verification par les moderateurs")
    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<SubmissionResponse>> createSubmission(
            @Valid @RequestBody VehicleSubmissionRequest request,
            HttpSession session) {

        User user = getCurrentUser(session);
        VehicleSubmission submission = submissionService.create(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(SubmissionMapper.toResponse(submission), "Submission created."));
    }

    @Operation(summary = "Mes soumissions", description = "Liste les soumissions de l'utilisateur connecte")
    @GetMapping("/submissions/mine")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getMySubmissions(HttpSession session) {
        User user = getCurrentUser(session);
        List<VehicleSubmission> submissions = submissionService.findByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(SubmissionMapper.toResponseList(submissions)));
    }

    @Operation(summary = "Detail d'une soumission", description = "Recupere une soumission par son ID (proprietaire ou moderateur)")
    @GetMapping("/submissions/{id}")
    public ResponseEntity<ApiResponse<SubmissionResponse>> getSubmission(
            @Parameter(description = "ID de la soumission") @PathVariable Long id,
            HttpSession session) {

        User user = getCurrentUser(session);
        VehicleSubmission submission = submissionService.findById(id);

        // Verifier que l'utilisateur est le proprietaire ou un moderateur
        if (!submission.getSubmitter().getId().equals(user.getId()) && !user.getIsModerator()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied.", "FORBIDDEN"));
        }

        return ResponseEntity.ok(ApiResponse.success(SubmissionMapper.toResponse(submission)));
    }

    // ==================== Endpoints Moderation ====================

    @Operation(summary = "Liste des soumissions (moderation)", description = "Liste les soumissions par statut pour les moderateurs (avec pagination optionnelle)")
    @GetMapping("/moderation/submissions")
    public ResponseEntity<ApiResponse<?>> getSubmissionsByStatus(
            @Parameter(description = "Statut des soumissions") @RequestParam(defaultValue = "pending") SubmissionStatus status,
            @Parameter(description = "Numero de page (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Nombre d'elements par page") @RequestParam(required = false) Integer size) {

        // Si pas de pagination demandee, retourner toutes les soumissions
        if (page == null && size == null) {
            List<VehicleSubmission> submissions = submissionService.findByStatus(status);
            return ResponseEntity.ok(ApiResponse.success(SubmissionMapper.toResponseList(submissions)));
        }

        // Pagination avec limites de securite (max 100 elements par page)
        var pageable = PaginationUtils.createPageable(page, size, Sort.by("createdAt").descending());

        var pagedSubmissions = submissionService.findByStatus(status, pageable);
        var response = PagedResponse.from(pagedSubmissions, SubmissionMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Evaluer une soumission", description = "Approuve ou rejette une soumission de vehicule")
    @PostMapping("/moderation/submissions/{id}/review")
    public ResponseEntity<ApiResponse<SubmissionResponse>> reviewSubmission(
            @Parameter(description = "ID de la soumission") @PathVariable Long id,
            @Valid @RequestBody SubmissionReviewRequest request,
            HttpSession session) {

        User reviewer = getCurrentUser(session);
        VehicleSubmission submission;
        String message;

        if (Boolean.TRUE.equals(request.approved())) {
            submission = submissionService.approve(id, reviewer);
            message = "Submission approved.";
            audit.info("SUBMISSION_APPROVED reviewer_id={} submission_id={} submitter_id={}",
                    reviewer.getId(), submission.getId(), submission.getSubmitter().getId());
        } else {
            submission = submissionService.reject(id, reviewer, request.feedback());
            message = "Submission rejected.";
            audit.info("SUBMISSION_REJECTED reviewer_id={} submission_id={} submitter_id={} feedback={}",
                    reviewer.getId(), submission.getId(), submission.getSubmitter().getId(),
                    request.feedback() != null ? request.feedback() : "none");
        }

        return ResponseEntity.ok(ApiResponse.success(SubmissionMapper.toResponse(submission), message));
    }

    /**
     * Recupere l'utilisateur connecte depuis le contexte de securite.
     */
    private User getCurrentUser(HttpSession session) {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof User)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
        }
        return (User) authentication.getPrincipal();
    }
}
