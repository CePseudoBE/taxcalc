package be.hoffmann.backtaxes.dto.request;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour appliquer une indexation en masse.
 *
 * Exemple: pour appliquer +2% a tous les baremes TMC de Wallonie:
 * {
 *   "region": "wallonia",
 *   "taxType": "tmc",
 *   "indexationRate": 1.02,
 *   "effectiveDate": "2026-07-01"
 * }
 */
public record IndexationRequest(
        Region region,  // null = toutes les regions

        TaxType taxType,  // null = tous les types de taxes

        @NotNull(message = "Indexation rate is required (e.g., 1.02 for +2%)")
        BigDecimal indexationRate,

        @NotNull(message = "Effective date is required")
        LocalDate effectiveDate,

        boolean includeBrackets,  // indexer les baremes

        boolean includeParameters  // indexer les parametres
) {
    public IndexationRequest {
        // Par defaut, inclure les deux
        if (!includeBrackets && !includeParameters) {
            includeBrackets = true;
            includeParameters = true;
        }
    }
}
