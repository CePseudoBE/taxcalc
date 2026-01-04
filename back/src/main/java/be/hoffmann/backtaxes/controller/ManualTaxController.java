package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.ManualTaxCalculationRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.ManualTaxCalculationResponse;
import be.hoffmann.backtaxes.entity.PendingCalculation;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.service.AnalyticsService;
import be.hoffmann.backtaxes.service.ManualTaxCalculationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Calcul manuel", description = "Calcul de taxes avec donnees manuelles (sans variante)")
@RestController
@RequestMapping("/api/tax/manual")
public class ManualTaxController {

    private final ManualTaxCalculationService manualTaxCalculationService;
    private final AnalyticsService analyticsService;

    public ManualTaxController(
            ManualTaxCalculationService manualTaxCalculationService,
            AnalyticsService analyticsService) {
        this.manualTaxCalculationService = manualTaxCalculationService;
        this.analyticsService = analyticsService;
    }

    /**
     * Calcule les taxes avec des donnees manuelles.
     * POST /api/tax/manual/calculate
     */
    @PostMapping("/calculate")
    public ResponseEntity<ApiResponse<ManualTaxCalculationResponse>> calculateManual(
            @Valid @RequestBody ManualTaxCalculationRequest request,
            @RequestParam(defaultValue = "false") boolean saveForModeration,
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse) {

        UUID sessionId = analyticsService.getOrCreateSessionId(httpRequest, httpResponse);

        ManualTaxCalculationResponse result = manualTaxCalculationService.calculate(
                request, saveForModeration, sessionId);

        return ResponseEntity.ok(ApiResponse.success(result, result.getMessage()));
    }

    /**
     * Retourne les champs requis pour chaque region.
     * GET /api/tax/manual/requirements
     */
    @GetMapping("/requirements")
    public ResponseEntity<ApiResponse<AllRegionRequirements>> getRequirements() {
        return ResponseEntity.ok(ApiResponse.success(new AllRegionRequirements()));
    }

    /**
     * Retourne les champs requis pour une region specifique.
     * GET /api/tax/manual/requirements/{region}
     */
    @GetMapping("/requirements/{region}")
    public ResponseEntity<ApiResponse<RegionDetail>> getRequirementsForRegion(@PathVariable Region region) {
        return ResponseEntity.ok(ApiResponse.success(RegionDetail.forRegion(region)));
    }

    /**
     * Liste les calculs en attente de moderation.
     * GET /api/tax/manual/pending
     */
    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<PendingCalculation>>> getPendingCalculations(
            @RequestParam(required = false) Region region) {

        List<PendingCalculation> pending;
        if (region != null) {
            pending = manualTaxCalculationService.getPendingCalculationsByRegion(region);
        } else {
            pending = manualTaxCalculationService.getPendingCalculations();
        }
        return ResponseEntity.ok(ApiResponse.success(pending));
    }

    // ==================== RESPONSE CLASSES ====================

    public static class AllRegionRequirements {
        public final RegionDetail brussels = RegionDetail.forRegion(Region.brussels);
        public final RegionDetail flanders = RegionDetail.forRegion(Region.flanders);
        public final RegionDetail wallonia = RegionDetail.forRegion(Region.wallonia);
    }

    public static class RegionDetail {
        public String region;
        public String displayName;
        public String description;
        public TaxRequirements tmc;
        public TaxRequirements annual;

        public static RegionDetail forRegion(Region region) {
            RegionDetail detail = new RegionDetail();
            detail.region = region.name();

            switch (region) {
                case brussels -> {
                    detail.displayName = "Bruxelles";
                    detail.description = "Region la plus simple: seuls les CV fiscaux ou la puissance sont requis pour la TMC.";
                    detail.tmc = new TaxRequirements(
                            "fiscalHp OU powerKw",
                            List.of(
                                    new FieldInfo("fiscalHp", "CV fiscaux", "Puissance fiscale du vehicule", true),
                                    new FieldInfo("powerKw", "Puissance kW", "Puissance en kilowatts (alternatif aux CV)", true)
                            ),
                            "Au moins un des deux champs est requis"
                    );
                    detail.annual = new TaxRequirements(
                            "fiscalHp",
                            List.of(new FieldInfo("fiscalHp", "CV fiscaux", "Puissance fiscale du vehicule", false)),
                            null
                    );
                }
                case flanders -> {
                    detail.displayName = "Flandre";
                    detail.description = "Requiert la puissance, la norme Euro et les emissions CO2.";
                    detail.tmc = new TaxRequirements(
                            "(fiscalHp OU powerKw) + euroNorm + co2Wltp",
                            List.of(
                                    new FieldInfo("fiscalHp", "CV fiscaux", "Puissance fiscale du vehicule", true),
                                    new FieldInfo("powerKw", "Puissance kW", "Puissance en kilowatts (alternatif aux CV)", true),
                                    new FieldInfo("euroNorm", "Norme Euro", "euro_1 a euro_7", false),
                                    new FieldInfo("co2Wltp", "CO2 WLTP", "Emissions CO2 en g/km (cycle WLTP)", false)
                            ),
                            "fiscalHp/powerKw: au moins un requis"
                    );
                    detail.annual = new TaxRequirements(
                            "fiscalHp",
                            List.of(new FieldInfo("fiscalHp", "CV fiscaux", "Puissance fiscale du vehicule", false)),
                            null
                    );
                }
                case wallonia -> {
                    detail.displayName = "Wallonie";
                    detail.description = "Region la plus complete: requiert puissance, carburant, CO2 et poids.";
                    detail.tmc = new TaxRequirements(
                            "powerKw + fuel + co2Wltp + mmaKg",
                            List.of(
                                    new FieldInfo("powerKw", "Puissance kW", "Puissance en kilowatts", false),
                                    new FieldInfo("fuel", "Carburant", "petrol, diesel, electric, hybrid_petrol, etc.", false),
                                    new FieldInfo("co2Wltp", "CO2 WLTP", "Emissions CO2 en g/km (cycle WLTP)", false),
                                    new FieldInfo("mmaKg", "MMA", "Masse Maximale Autorisee en kg", false)
                            ),
                            null
                    );
                    detail.annual = new TaxRequirements(
                            "fiscalHp",
                            List.of(new FieldInfo("fiscalHp", "CV fiscaux", "Puissance fiscale du vehicule", false)),
                            null
                    );
                }
            }

            return detail;
        }
    }

    public static class TaxRequirements {
        public String formula;
        public List<FieldInfo> fields;
        public String note;

        public TaxRequirements(String formula, List<FieldInfo> fields, String note) {
            this.formula = formula;
            this.fields = fields;
            this.note = note;
        }
    }

    public static class FieldInfo {
        public String name;
        public String label;
        public String description;
        public boolean alternative;

        public FieldInfo(String name, String label, String description, boolean alternative) {
            this.name = name;
            this.label = label;
            this.description = description;
            this.alternative = alternative;
        }
    }
}
