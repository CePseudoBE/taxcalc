package be.hoffmann.backtaxes.dto.response;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;

/**
 * Reponse pour une variante de vehicule (version resumee).
 */
public record VariantResponse(
        Long id,
        String name,
        Integer yearStart,
        Integer yearEnd,
        Integer powerKw,
        Integer fiscalHp,
        FuelType fuel,
        EuroNorm euroNorm,
        Integer co2Wltp
) {
}
