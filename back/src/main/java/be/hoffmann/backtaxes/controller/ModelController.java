package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.mapper.ModelMapper;
import be.hoffmann.backtaxes.dto.mapper.VariantMapper;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.ModelResponse;
import be.hoffmann.backtaxes.dto.response.VariantResponse;
import be.hoffmann.backtaxes.entity.Model;
import be.hoffmann.backtaxes.service.ModelService;
import be.hoffmann.backtaxes.service.VariantService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller pour les modeles de vehicules.
 * Endpoints publics (pas d'authentification requise).
 */
@Tag(name = "Modeles", description = "Catalogue des modeles de vehicules")
@RestController
@RequestMapping("/api/models")
public class ModelController {

    private final ModelService modelService;
    private final VariantService variantService;

    public ModelController(ModelService modelService, VariantService variantService) {
        this.modelService = modelService;
        this.variantService = variantService;
    }

    @Operation(summary = "Recherche de modeles", description = "Recherche de modeles par mot-cle dans le nom")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ModelResponse>>> searchModels(
            @Parameter(description = "Mot-cle de recherche") @RequestParam(required = false) String search) {

        List<Model> models = (search != null && !search.isBlank())
                ? modelService.searchByName(search)
                : new ArrayList<>();
        return ResponseEntity.ok(ApiResponse.success(ModelMapper.toResponseList(models)));
    }

    @Operation(summary = "Recupere un modele", description = "Retourne les details d'un modele par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ModelResponse>> getModelById(@Parameter(description = "ID du modele") @PathVariable Long id) {
        var model = modelService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(ModelMapper.toResponse(model)));
    }

    @Operation(summary = "Liste les variantes d'un modele", description = "Retourne toutes les variantes associees a un modele")
    @GetMapping("/{id}/variants")
    public ResponseEntity<ApiResponse<List<VariantResponse>>> getVariantsByModel(@Parameter(description = "ID du modele") @PathVariable Long id) {
        var variants = variantService.findByModelId(id);
        return ResponseEntity.ok(ApiResponse.success(VariantMapper.toResponseList(variants)));
    }
}
