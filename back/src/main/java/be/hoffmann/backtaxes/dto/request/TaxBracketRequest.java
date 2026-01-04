package be.hoffmann.backtaxes.dto.request;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour creer/modifier un bareme de taxe.
 */
public record TaxBracketRequest(
        @NotNull(message = "Region is required")
        Region region,

        @NotNull(message = "Tax type is required")
        TaxType taxType,

        @NotNull(message = "Bracket key is required")
        String bracketKey,

        @NotNull(message = "Min value is required")
        Integer minValue,

        Integer maxValue,

        @NotNull(message = "Amount is required")
        BigDecimal amount,

        @NotNull(message = "Valid from date is required")
        LocalDate validFrom,

        LocalDate validTo
) {}
