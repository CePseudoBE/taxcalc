package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.mapper.VariantMapper;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.VariantDetailResponse;
import be.hoffmann.backtaxes.dto.response.VariantResponse;
import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.service.VariantService;
import be.hoffmann.backtaxes.service.VariantService.VariantSearchCriteria;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pour les variantes de vehicules.
 * Endpoints publics (pas d'authentification requise).
 */
@Tag(name = "Variantes", description = "Catalogue des variantes de vehicules (motorisations)")
@RestController
@RequestMapping("/api/variants")
public class VariantController {

    private final VariantService variantService;

    public VariantController(VariantService variantService) {
        this.variantService = variantService;
    }

    @Operation(summary = "Recherche de variantes", description = "Recherche de variantes avec filtres multiples. Au moins un filtre est requis.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<VariantResponse>>> searchVariants(
            @Parameter(description = "ID de la marque") @RequestParam(required = false) Long brandId,
            @Parameter(description = "ID du modele") @RequestParam(required = false) Long modelId,
            @Parameter(description = "Types de carburant") @RequestParam(required = false) List<FuelType> fuel,
            @Parameter(description = "Normes Euro") @RequestParam(required = false) List<EuroNorm> euroNorm,
            @Parameter(description = "Puissance minimale (CV)") @RequestParam(required = false) Integer minPower,
            @Parameter(description = "Puissance maximale (CV)") @RequestParam(required = false) Integer maxPower,
            @Parameter(description = "Annee de debut minimum") @RequestParam(required = false) Integer minYear,
            @Parameter(description = "Annee de fin maximum") @RequestParam(required = false) Integer maxYear,
            @Parameter(description = "Emissions CO2 max (g/km)") @RequestParam(required = false) Integer maxCo2) {

        // Retourne une liste vide si aucun filtre n'est fourni
        boolean hasFilters = brandId != null || modelId != null ||
                (fuel != null && !fuel.isEmpty()) ||
                (euroNorm != null && !euroNorm.isEmpty()) ||
                minPower != null || maxPower != null ||
                minYear != null || maxYear != null || maxCo2 != null;

        if (!hasFilters) {
            return ResponseEntity.ok(ApiResponse.success(List.of()));
        }

        VariantSearchCriteria criteria = new VariantSearchCriteria();
        criteria.setBrandId(brandId);
        criteria.setModelId(modelId);
        criteria.setFuelTypes(fuel != null && !fuel.isEmpty() ? fuel : null);
        criteria.setEuroNorms(euroNorm != null && !euroNorm.isEmpty() ? euroNorm : null);
        criteria.setMinPower(minPower);
        criteria.setMaxPower(maxPower);
        criteria.setMinYear(minYear);
        criteria.setMaxYear(maxYear);
        criteria.setMaxCo2(maxCo2);

        var variants = variantService.search(criteria);
        return ResponseEntity.ok(ApiResponse.success(VariantMapper.toResponseList(variants)));
    }

    @Operation(summary = "Recupere une variante", description = "Retourne les details complets d'une variante par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<VariantDetailResponse>> getVariantById(@Parameter(description = "ID de la variante") @PathVariable Long id) {
        // Utilise findByIdWithDetails pour eviter le probleme N+1
        var variant = variantService.findByIdWithDetails(id);
        return ResponseEntity.ok(ApiResponse.success(VariantMapper.toDetailResponse(variant)));
    }
}
