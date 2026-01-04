package be.hoffmann.backtaxes.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Requete pour la moderation d'une soumission de vehicule.
 */
public record SubmissionReviewRequest(
        @NotNull(message = "Approval status is required")
        Boolean approved,

        @Size(max = 1000, message = "Feedback must not exceed 1000 characters")
        String feedback
) {
}
