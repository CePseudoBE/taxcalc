package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.mapper.BrandMapper;
import be.hoffmann.backtaxes.dto.mapper.ModelMapper;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.BrandResponse;
import be.hoffmann.backtaxes.dto.response.ModelResponse;
import be.hoffmann.backtaxes.dto.response.PagedResponse;
import be.hoffmann.backtaxes.service.BrandService;
import be.hoffmann.backtaxes.service.ModelService;
import be.hoffmann.backtaxes.util.PaginationUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Sort;
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

    @Operation(summary = "Liste toutes les marques", description = "Retourne la liste des marques automobiles (avec pagination optionnelle)")
    @GetMapping
    public ResponseEntity<ApiResponse<?>> getAllBrands(
            @Parameter(description = "Numero de page (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Nombre d'elements par page") @RequestParam(required = false) Integer size) {

        // Si pas de pagination demandee, retourner toutes les marques
        if (page == null && size == null) {
            var brands = brandService.findAll();
            return ResponseEntity.ok(ApiResponse.success(BrandMapper.toResponseList(brands)));
        }

        // Pagination avec limites de securite (max 100 elements par page)
        var pageable = PaginationUtils.createPageable(page, size, Sort.by("name").ascending());

        var pagedBrands = brandService.findAll(pageable);
        var response = PagedResponse.from(pagedBrands, BrandMapper::toResponse);

        return ResponseEntity.ok(ApiResponse.success(response));
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
            @Parameter(description = "Recherche par nom de modele (max 100 caracteres)") @RequestParam(required = false) String search) {

        // Limite la recherche a 100 caracteres pour eviter les abus
        var sanitizedSearch = (search != null && !search.isBlank())
                ? search.substring(0, Math.min(search.length(), 100)).trim()
                : null;

        var models = (sanitizedSearch != null)
                ? modelService.searchByBrandAndName(id, sanitizedSearch)
                : modelService.findByBrandId(id);
        return ResponseEntity.ok(ApiResponse.success(ModelMapper.toResponseList(models)));
    }
}
