package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.mapper.BrandMapper;
import be.hoffmann.backtaxes.dto.mapper.ModelMapper;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.BrandResponse;
import be.hoffmann.backtaxes.dto.response.ModelResponse;
import be.hoffmann.backtaxes.service.BrandService;
import be.hoffmann.backtaxes.service.ModelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller pour les marques de vehicules.
 * Endpoints publics (pas d'authentification requise).
 */
@Tag(name = "Marques", description = "Catalogue des marques automobiles")
@RestController
@RequestMapping("/api/brands")
public class BrandController {

    private final BrandService brandService;
    private final ModelService modelService;

    public BrandController(BrandService brandService, ModelService modelService) {
        this.brandService = brandService;
        this.modelService = modelService;
    }

    @Operation(summary = "Liste toutes les marques", description = "Retourne la liste complete des marques automobiles")
    @GetMapping
    public ResponseEntity<ApiResponse<List<BrandResponse>>> getAllBrands() {
        var brands = brandService.findAll();
        return ResponseEntity.ok(ApiResponse.success(BrandMapper.toResponseList(brands)));
    }

    @Operation(summary = "Recupere une marque", description = "Retourne les details d'une marque par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BrandResponse>> getBrandById(@Parameter(description = "ID de la marque") @PathVariable Long id) {
        var brand = brandService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(BrandMapper.toResponse(brand)));
    }

    @Operation(summary = "Liste les modeles d'une marque", description = "Retourne les modeles associes a une marque, avec recherche optionnelle")
    @GetMapping("/{id}/models")
    public ResponseEntity<ApiResponse<List<ModelResponse>>> getModelsByBrand(
            @Parameter(description = "ID de la marque") @PathVariable Long id,
            @Parameter(description = "Recherche par nom de modele") @RequestParam(required = false) String search) {

        var models = (search != null && !search.isBlank())
                ? modelService.searchByBrandAndName(id, search)
                : modelService.findByBrandId(id);
        return ResponseEntity.ok(ApiResponse.success(ModelMapper.toResponseList(models)));
    }
}
