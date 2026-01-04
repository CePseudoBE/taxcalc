package be.hoffmann.backtaxes.dto.request;

import be.hoffmann.backtaxes.entity.enums.Region;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * Requete pour sauvegarder une recherche/calcul de taxe.
 * Doit contenir soit variantId soit submissionId (XOR).
 */
public class SavedSearchRequest {

    private Long variantId;

    private Long submissionId;

    @NotNull(message = "Region is required")
    private Region region;

    private LocalDate firstRegistrationDate;

    @Size(max = 100, message = "Label must not exceed 100 characters")
    private String label;

    public SavedSearchRequest() {
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public LocalDate getFirstRegistrationDate() {
        return firstRegistrationDate;
    }

    public void setFirstRegistrationDate(LocalDate firstRegistrationDate) {
        this.firstRegistrationDate = firstRegistrationDate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * Valide que soit variantId soit submissionId est present.
     */
    public boolean hasValidVehicleReference() {
        return (variantId != null) != (submissionId != null);
    }
}
