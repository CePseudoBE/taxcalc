package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.mapper.SubmissionMapper;
import be.hoffmann.backtaxes.dto.request.SubmissionReviewRequest;
import be.hoffmann.backtaxes.dto.request.VehicleSubmissionRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.SubmissionResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.entity.VehicleSubmission;
import be.hoffmann.backtaxes.entity.enums.SubmissionStatus;
import be.hoffmann.backtaxes.service.SubmissionService;
import be.hoffmann.backtaxes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Soumissions", description = "Soumission et moderation de nouveaux vehicules")
@RestController
@RequestMapping("/api")
public class SubmissionController {

    private static final String USER_SESSION_KEY = "user_id";

    private final SubmissionService submissionService;
    private final UserService userService;

    public SubmissionController(SubmissionService submissionService, UserService userService) {
        this.submissionService = submissionService;
        this.userService = userService;
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

    @Operation(summary = "Liste des soumissions (moderation)", description = "Liste les soumissions par statut pour les moderateurs")
    @GetMapping("/moderation/submissions")
    public ResponseEntity<ApiResponse<List<SubmissionResponse>>> getSubmissionsByStatus(
            @Parameter(description = "Statut des soumissions") @RequestParam(defaultValue = "pending") SubmissionStatus status) {

        List<VehicleSubmission> submissions = submissionService.findByStatus(status);
        return ResponseEntity.ok(ApiResponse.success(SubmissionMapper.toResponseList(submissions)));
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
        } else {
            submission = submissionService.reject(id, reviewer, request.feedback());
            message = "Submission rejected.";
        }

        return ResponseEntity.ok(ApiResponse.success(SubmissionMapper.toResponse(submission), message));
    }

    /**
     * Recupere l'utilisateur connecte depuis la session.
     * En mode dev local (sans auth), utilise l'utilisateur ID 1 par defaut.
     */
    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_SESSION_KEY);
        if (userId == null) {
            // Mode dev: tenter d'utiliser l'utilisateur par defaut (ID 1)
            try {
                return userService.findById(1L);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
        }
        return userService.findById(userId);
    }
}
