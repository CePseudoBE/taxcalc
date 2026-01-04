package be.hoffmann.backtaxes.dto.response;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.SubmissionStatus;

import java.time.Instant;

/**
 * Reponse pour une soumission de vehicule.
 */
public record SubmissionResponse(
        Long id,
        SubmissionStatus status,
        VehicleData vehicleData,
        Long submitterId,
        Instant submittedAt,
        Long reviewedById,
        Instant reviewedAt,
        String feedback,
        Long createdVariantId
) {
    /**
     * Donnees du vehicule soumis.
     */
    public record VehicleData(
            String brandName,
            String modelName,
            String variantName,
            Integer yearStart,
            Integer yearEnd,
            Integer powerKw,
            Integer fiscalHp,
            FuelType fuel,
            EuroNorm euroNorm,
            Integer co2Wltp,
            Integer co2Nedc,
            Integer displacementCc,
            Integer mmaKg,
            Boolean hasParticleFilter
    ) {
    }
}
