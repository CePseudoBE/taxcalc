package be.hoffmann.backtaxes.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO de reponse apres application d'une indexation.
 */
public record IndexationResponse(
        int bracketsUpdated,
        int parametersUpdated,
        BigDecimal indexationRate,
        LocalDate effectiveDate,
        String message
) {}
