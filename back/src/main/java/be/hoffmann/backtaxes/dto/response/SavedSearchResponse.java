package be.hoffmann.backtaxes.dto.response;

import be.hoffmann.backtaxes.entity.enums.Region;

import java.time.Instant;
import java.time.LocalDate;

/**
 * Reponse pour une recherche sauvegardee.
 */
public record SavedSearchResponse(
        Long id,
        String label,
        Region region,
        LocalDate firstRegistrationDate,
        VehicleSummary vehicle,
        Instant createdAt
) {
    /**
     * Resume du vehicule (variante ou soumission).
     */
    public record VehicleSummary(
            Long id,
            String type,
            String brandName,
            String modelName,
            String variantName,
            Integer powerKw,
            String fuel
    ) {
    }
}
