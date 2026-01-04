package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.IndexationRequest;
import be.hoffmann.backtaxes.dto.request.TaxBracketRequest;
import be.hoffmann.backtaxes.dto.request.TaxParameterRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.IndexationResponse;
import be.hoffmann.backtaxes.dto.response.TaxBracketResponse;
import be.hoffmann.backtaxes.dto.response.TaxParameterResponse;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.service.AdminTaxService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administration taxes", description = "Gestion des baremes et parametres de taxes (admin)")
@RestController
@RequestMapping("/api/admin/tax")
public class AdminTaxController {

    private final AdminTaxService adminTaxService;

    public AdminTaxController(AdminTaxService adminTaxService) {
        this.adminTaxService = adminTaxService;
    }

    // ==================== TAX BRACKETS ====================

    /**
     * Liste tous les baremes.
     * GET /api/admin/tax/brackets
     */
    @GetMapping("/brackets")
    public ResponseEntity<ApiResponse<List<TaxBracketResponse>>> getAllBrackets() {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.getAllBrackets()));
    }

    /**
     * Liste les baremes par region et type.
     * GET /api/admin/tax/brackets?region=wallonia&taxType=tmc
     */
    @GetMapping("/brackets/filter")
    public ResponseEntity<ApiResponse<List<TaxBracketResponse>>> getBracketsByRegionAndType(
            @RequestParam Region region,
            @RequestParam TaxType taxType) {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.getBracketsByRegionAndType(region, taxType)));
    }

    /**
     * Recupere un bareme par ID.
     * GET /api/admin/tax/brackets/{id}
     */
    @GetMapping("/brackets/{id}")
    public ResponseEntity<ApiResponse<TaxBracketResponse>> getBracketById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.getBracketById(id)));
    }

    /**
     * Cree un nouveau bareme.
     * POST /api/admin/tax/brackets
     */
    @PostMapping("/brackets")
    public ResponseEntity<ApiResponse<TaxBracketResponse>> createBracket(
            @Valid @RequestBody TaxBracketRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(adminTaxService.createBracket(request), "Bracket created."));
    }

    /**
     * Met a jour un bareme existant.
     * PUT /api/admin/tax/brackets/{id}
     */
    @PutMapping("/brackets/{id}")
    public ResponseEntity<ApiResponse<TaxBracketResponse>> updateBracket(
            @PathVariable Long id,
            @Valid @RequestBody TaxBracketRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.updateBracket(id, request), "Bracket updated."));
    }

    /**
     * Supprime un bareme.
     * DELETE /api/admin/tax/brackets/{id}
     */
    @DeleteMapping("/brackets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteBracket(@PathVariable Long id) {
        adminTaxService.deleteBracket(id);
        return ResponseEntity.ok(ApiResponse.success("Bracket deleted."));
    }

    // ==================== TAX PARAMETERS ====================

    /**
     * Liste tous les parametres.
     * GET /api/admin/tax/parameters
     */
    @GetMapping("/parameters")
    public ResponseEntity<ApiResponse<List<TaxParameterResponse>>> getAllParameters() {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.getAllParameters()));
    }

    /**
     * Liste les parametres par region et type.
     * GET /api/admin/tax/parameters/filter?region=brussels&taxType=tmc
     */
    @GetMapping("/parameters/filter")
    public ResponseEntity<ApiResponse<List<TaxParameterResponse>>> getParametersByRegionAndType(
            @RequestParam Region region,
            @RequestParam TaxType taxType) {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.getParametersByRegionAndType(region, taxType)));
    }

    /**
     * Recupere un parametre par ID.
     * GET /api/admin/tax/parameters/{id}
     */
    @GetMapping("/parameters/{id}")
    public ResponseEntity<ApiResponse<TaxParameterResponse>> getParameterById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.getParameterById(id)));
    }

    /**
     * Cree un nouveau parametre.
     * POST /api/admin/tax/parameters
     */
    @PostMapping("/parameters")
    public ResponseEntity<ApiResponse<TaxParameterResponse>> createParameter(
            @Valid @RequestBody TaxParameterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(adminTaxService.createParameter(request), "Parameter created."));
    }

    /**
     * Met a jour un parametre existant.
     * PUT /api/admin/tax/parameters/{id}
     */
    @PutMapping("/parameters/{id}")
    public ResponseEntity<ApiResponse<TaxParameterResponse>> updateParameter(
            @PathVariable Long id,
            @Valid @RequestBody TaxParameterRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.updateParameter(id, request), "Parameter updated."));
    }

    /**
     * Supprime un parametre.
     * DELETE /api/admin/tax/parameters/{id}
     */
    @DeleteMapping("/parameters/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteParameter(@PathVariable Long id) {
        adminTaxService.deleteParameter(id);
        return ResponseEntity.ok(ApiResponse.success("Parameter deleted."));
    }

    // ==================== INDEXATION ====================

    /**
     * Applique une indexation en masse.
     * POST /api/admin/tax/indexation
     *
     * Exemple de body:
     * {
     *   "region": "wallonia",        // null = toutes les regions
     *   "taxType": "annual",         // null = tous les types
     *   "indexationRate": 1.02,      // +2%
     *   "effectiveDate": "2026-07-01",
     *   "includeBrackets": true,
     *   "includeParameters": true
     * }
     */
    @PostMapping("/indexation")
    public ResponseEntity<ApiResponse<IndexationResponse>> applyIndexation(
            @Valid @RequestBody IndexationRequest request) {
        return ResponseEntity.ok(ApiResponse.success(adminTaxService.applyIndexation(request), "Indexation applied."));
    }
}
