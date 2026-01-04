package be.hoffmann.backtaxes.dto.response;

import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de reponse pour un bareme de taxe.
 */
public record TaxBracketResponse(
        Long id,
        Region region,
        TaxType taxType,
        String bracketKey,
        Integer minValue,
        Integer maxValue,
        BigDecimal amount,
        LocalDate validFrom,
        LocalDate validTo
) {
    public static TaxBracketResponse from(TaxBracket entity) {
        return new TaxBracketResponse(
                entity.getId(),
                entity.getRegion(),
                entity.getTaxType(),
                entity.getBracketKey(),
                entity.getMinValue(),
                entity.getMaxValue(),
                entity.getAmount(),
                entity.getValidFrom(),
                entity.getValidTo()
        );
    }
}
