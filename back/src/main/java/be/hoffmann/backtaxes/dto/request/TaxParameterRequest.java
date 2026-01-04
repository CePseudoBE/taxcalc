package be.hoffmann.backtaxes.dto.request;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO pour creer/modifier un parametre de taxe.
 */
public record TaxParameterRequest(
        @NotNull(message = "Region is required")
        Region region,

        @NotNull(message = "Tax type is required")
        TaxType taxType,

        @NotNull(message = "Parameter key is required")
        String paramKey,

        @NotNull(message = "Parameter value is required")
        BigDecimal paramValue,

        @NotNull(message = "Valid from date is required")
        LocalDate validFrom,

        LocalDate validTo
) {}
