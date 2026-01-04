package be.hoffmann.backtaxes.dto.request;

import be.hoffmann.backtaxes.entity.enums.Region;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

/**
 * Requete pour calculer les taxes d'un vehicule.
 * Doit contenir soit variantId soit submissionId (XOR).
 */
public class TaxCalculationRequest {

    private Long variantId;

    private Long submissionId;

    @NotNull(message = "Region is required")
    private Region region;

    @NotNull(message = "La date de première immatriculation est obligatoire")
    @Valid
    private FirstRegistrationDate firstRegistrationDate;

    public TaxCalculationRequest() {
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

    public FirstRegistrationDate getFirstRegistrationDate() {
        return firstRegistrationDate;
    }

    public void setFirstRegistrationDate(FirstRegistrationDate firstRegistrationDate) {
        this.firstRegistrationDate = firstRegistrationDate;
    }

    /**
     * Valide que soit variantId soit submissionId est present.
     */
    public boolean hasValidVehicleReference() {
        return (variantId != null) != (submissionId != null);
    }
}
