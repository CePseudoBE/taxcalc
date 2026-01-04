package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.TaxCalculationRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.TaxCalculationResponse;
import be.hoffmann.backtaxes.entity.enums.SearchType;
import be.hoffmann.backtaxes.service.AnalyticsService;
import be.hoffmann.backtaxes.service.TaxCalculationService;
import be.hoffmann.backtaxes.service.TaxCalculationService.TaxCalculationResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Controller pour le calcul des taxes vehicules.
 * Endpoint public (pas d'authentification requise).
 */
@Tag(name = "Taxes", description = "Calcul des taxes automobiles (TMC et taxe annuelle)")
@RestController
@RequestMapping("/api/tax")
public class TaxController {

    private final TaxCalculationService taxCalculationService;
    private final AnalyticsService analyticsService;

    public TaxController(
            TaxCalculationService taxCalculationService,
            AnalyticsService analyticsService) {
        this.taxCalculationService = taxCalculationService;
        this.analyticsService = analyticsService;
    }

    @Operation(summary = "Calcule TMC et taxe annuelle", description = "Calcule les deux types de taxes pour un vehicule donne")
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<Map<String, TaxCalculationResponse>>> calculateTax(
            @Valid @RequestBody TaxCalculationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Calculer les taxes
        TaxCalculationResult result = taxCalculationService.calculateBoth(request);

        // Logger l'evenement analytics (async)
        logCalculationEvent(request, httpRequest, httpResponse);

        // Construire la reponse
        Map<String, TaxCalculationResponse> response = new HashMap<>();
        response.put("tmc", result.getTmc());
        response.put("annual", result.getAnnual());

        return ResponseEntity.ok(ApiResponse.success(response, "Tax calculation completed."));
    }

    @Operation(summary = "Calcule la TMC", description = "Calcule la Taxe de Mise en Circulation uniquement")
    @PostMapping("/tmc")
    public ResponseEntity<ApiResponse<TaxCalculationResponse>> calculateTmc(
            @Valid @RequestBody TaxCalculationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Utilise calculateTmcOnly pour eviter de calculer la taxe annuelle inutilement
        TaxCalculationResponse result = taxCalculationService.calculateTmcOnly(request);
        logCalculationEvent(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success(result, "TMC calculation completed."));
    }

    @Operation(summary = "Calcule la taxe annuelle", description = "Calcule la taxe de circulation annuelle uniquement")
    @PostMapping("/annual")
    public ResponseEntity<ApiResponse<TaxCalculationResponse>> calculateAnnual(
            @Valid @RequestBody TaxCalculationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        // Utilise calculateAnnualOnly pour eviter de calculer la TMC inutilement
        TaxCalculationResponse result = taxCalculationService.calculateAnnualOnly(request);
        logCalculationEvent(request, httpRequest, httpResponse);
        return ResponseEntity.ok(ApiResponse.success(result, "Annual tax calculation completed."));
    }

    /**
     * Enregistre l'evenement de calcul pour analytics.
     */
    private void logCalculationEvent(
            TaxCalculationRequest request,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        UUID sessionId = analyticsService.getOrCreateSessionId(httpRequest, httpResponse);
        String userAgent = httpRequest.getHeader("User-Agent");
        String referer = httpRequest.getHeader("Referer");
        String language = httpRequest.getHeader("Accept-Language");

        analyticsService.logSearch(
                analyticsService.builder()
                        .sessionId(sessionId)
                        .variantId(request.getVariantId())
                        .region(request.getRegion())
                        .firstRegistrationDate(request.getFirstRegistrationDate().toLocalDate())
                        .searchType(SearchType.calculate)
                        .deviceType(analyticsService.detectDeviceType(userAgent))
                        .referrerSource(analyticsService.extractReferrerSource(referer))
                        .userAgentHash(analyticsService.hashUserAgent(userAgent))
                        .language(language != null ? language.substring(0, Math.min(10, language.length())) : null)
        );
    }
}
