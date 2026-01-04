package be.hoffmann.backtaxes.dto.response;

import be.hoffmann.backtaxes.entity.TaxParameter;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de reponse pour un parametre de taxe.
 */
public record TaxParameterResponse(
        Long id,
        Region region,
        TaxType taxType,
        String paramKey,
        BigDecimal paramValue,
        LocalDate validFrom,
        LocalDate validTo
) {
    public static TaxParameterResponse from(TaxParameter entity) {
        return new TaxParameterResponse(
                entity.getId(),
                entity.getRegion(),
                entity.getTaxType(),
                entity.getParamKey(),
                entity.getParamValue(),
                entity.getValidFrom(),
                entity.getValidTo()
        );
    }
}
