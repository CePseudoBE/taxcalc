package be.hoffmann.backtaxes.dto.response;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;

/**
 * Reponse detaillee pour une variante de vehicule.
 * Inclut toutes les specifications techniques.
 */
public record VariantDetailResponse(
        Long id,
        String name,
        ModelResponse model,
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
