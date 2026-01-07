package be.hoffmann.backtaxes.controller.admin;

import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.PagedResponse;
import be.hoffmann.backtaxes.entity.AgeCoefficient;
import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.TaxExemption;
import be.hoffmann.backtaxes.entity.TaxParameter;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.repository.AgeCoefficientRepository;
import be.hoffmann.backtaxes.repository.TaxBracketRepository;
import be.hoffmann.backtaxes.repository.TaxExemptionRepository;
import be.hoffmann.backtaxes.repository.TaxParameterRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Administration", description = "Configuration des taxes (acces administrateur)")
@RestController
@RequestMapping("/api/admin")
public class TaxConfigController {

    private final TaxBracketRepository taxBracketRepository;
    private final TaxParameterRepository taxParameterRepository;
    private final AgeCoefficientRepository ageCoefficientRepository;
    private final TaxExemptionRepository taxExemptionRepository;

    public TaxConfigController(
            TaxBracketRepository taxBracketRepository,
            TaxParameterRepository taxParameterRepository,
            AgeCoefficientRepository ageCoefficientRepository,
            TaxExemptionRepository taxExemptionRepository) {
        this.taxBracketRepository = taxBracketRepository;
        this.taxParameterRepository = taxParameterRepository;
        this.ageCoefficientRepository = ageCoefficientRepository;
        this.taxExemptionRepository = taxExemptionRepository;
    }

    // ==================== Tax Brackets ====================

    @Operation(summary = "Liste des tranches", description = "Liste les tranches de taxes avec filtres optionnels et pagination optionnelle")
    @GetMapping("/tax-brackets")
    public ResponseEntity<ApiResponse<?>> getAllTaxBrackets(
            @Parameter(description = "Region") @RequestParam(required = false) Region region,
            @Parameter(description = "Type de taxe") @RequestParam(required = false) TaxType taxType,
            @Parameter(description = "Numero de page (0-indexed)") @RequestParam(required = false) Integer page,
            @Parameter(description = "Nombre d'elements par page") @RequestParam(required = false) Integer size) {

        // Si pas de pagination demandee, retourner toutes les tranches
        if (page == null && size == null) {
            List<TaxBracket> brackets = (region != null || taxType != null)
                    ? taxBracketRepository.findByFilters(region, taxType, PageRequest.of(0, Integer.MAX_VALUE)).getContent()
                    : taxBracketRepository.findAll();
            return ResponseEntity.ok(ApiResponse.success(brackets));
        }

        // Pagination avec valeurs par defaut
        int pageNum = page != null ? page : 0;
        int pageSize = size != null ? size : 50;
        var pageable = PageRequest.of(pageNum, pageSize, Sort.by("region", "taxType", "bracketKey", "minValue").ascending());

        var pagedBrackets = taxBracketRepository.findByFilters(region, taxType, pageable);
        var response = PagedResponse.from(pagedBrackets);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @Operation(summary = "Detail d'une tranche", description = "Recupere une tranche de taxe par son ID")
    @GetMapping("/tax-brackets/{id}")
    public ResponseEntity<ApiResponse<TaxBracket>> getTaxBracket(@Parameter(description = "ID de la tranche") @PathVariable Long id) {
        TaxBracket bracket = taxBracketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxBracket", "id", id));
        return ResponseEntity.ok(ApiResponse.success(bracket));
    }

    @Operation(summary = "Creer une tranche", description = "Cree une nouvelle tranche de taxe")
    @PostMapping("/tax-brackets")
    public ResponseEntity<ApiResponse<TaxBracket>> createTaxBracket(@Valid @RequestBody TaxBracket bracket) {
        bracket.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taxBracketRepository.save(bracket), "Bracket created."));
    }

    @Operation(summary = "Modifier une tranche", description = "Met a jour une tranche de taxe existante")
    @PutMapping("/tax-brackets/{id}")
    public ResponseEntity<ApiResponse<TaxBracket>> updateTaxBracket(
            @Parameter(description = "ID de la tranche") @PathVariable Long id,
            @Valid @RequestBody TaxBracket bracket) {

        if (!taxBracketRepository.existsById(id)) {
            throw new ResourceNotFoundException("TaxBracket", "id", id);
        }
        bracket.setId(id);
        return ResponseEntity.ok(ApiResponse.success(taxBracketRepository.save(bracket), "Bracket updated."));
    }

    @Operation(summary = "Supprimer une tranche", description = "Supprime une tranche de taxe")
    @DeleteMapping("/tax-brackets/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTaxBracket(@Parameter(description = "ID de la tranche") @PathVariable Long id) {
        if (!taxBracketRepository.existsById(id)) {
            throw new ResourceNotFoundException("TaxBracket", "id", id);
        }
        taxBracketRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Bracket deleted."));
    }

    // ==================== Tax Parameters ====================

    @Operation(summary = "Liste des parametres", description = "Liste tous les parametres de taxes avec filtres optionnels")
    @GetMapping("/tax-parameters")
    public ResponseEntity<ApiResponse<List<TaxParameter>>> getAllTaxParameters(
            @Parameter(description = "Region") @RequestParam(required = false) Region region,
            @Parameter(description = "Type de taxe") @RequestParam(required = false) TaxType taxType) {

        List<TaxParameter> params = (region != null && taxType != null)
                ? taxParameterRepository.findByRegionAndTaxType(region, taxType)
                : taxParameterRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(params));
    }

    @Operation(summary = "Detail d'un parametre", description = "Recupere un parametre de taxe par son ID")
    @GetMapping("/tax-parameters/{id}")
    public ResponseEntity<ApiResponse<TaxParameter>> getTaxParameter(@Parameter(description = "ID du parametre") @PathVariable Long id) {
        TaxParameter param = taxParameterRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxParameter", "id", id));
        return ResponseEntity.ok(ApiResponse.success(param));
    }

    @Operation(summary = "Creer un parametre", description = "Cree un nouveau parametre de taxe")
    @PostMapping("/tax-parameters")
    public ResponseEntity<ApiResponse<TaxParameter>> createTaxParameter(@Valid @RequestBody TaxParameter param) {
        param.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taxParameterRepository.save(param), "Parameter created."));
    }

    @Operation(summary = "Modifier un parametre", description = "Met a jour un parametre de taxe existant")
    @PutMapping("/tax-parameters/{id}")
    public ResponseEntity<ApiResponse<TaxParameter>> updateTaxParameter(
            @Parameter(description = "ID du parametre") @PathVariable Long id,
            @Valid @RequestBody TaxParameter param) {

        if (!taxParameterRepository.existsById(id)) {
            throw new ResourceNotFoundException("TaxParameter", "id", id);
        }
        param.setId(id);
        return ResponseEntity.ok(ApiResponse.success(taxParameterRepository.save(param), "Parameter updated."));
    }

    @Operation(summary = "Supprimer un parametre", description = "Supprime un parametre de taxe")
    @DeleteMapping("/tax-parameters/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTaxParameter(@Parameter(description = "ID du parametre") @PathVariable Long id) {
        if (!taxParameterRepository.existsById(id)) {
            throw new ResourceNotFoundException("TaxParameter", "id", id);
        }
        taxParameterRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Parameter deleted."));
    }

    // ==================== Age Coefficients ====================

    @Operation(summary = "Liste des coefficients d'age", description = "Liste tous les coefficients de reduction par age")
    @GetMapping("/age-coefficients")
    public ResponseEntity<ApiResponse<List<AgeCoefficient>>> getAllAgeCoefficients(
            @Parameter(description = "Region") @RequestParam(required = false) Region region,
            @Parameter(description = "Type de taxe") @RequestParam(required = false) TaxType taxType) {

        List<AgeCoefficient> coefs = (region != null && taxType != null)
                ? ageCoefficientRepository.findByRegionAndTaxType(region, taxType)
                : ageCoefficientRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(coefs));
    }

    @Operation(summary = "Detail d'un coefficient", description = "Recupere un coefficient d'age par son ID")
    @GetMapping("/age-coefficients/{id}")
    public ResponseEntity<ApiResponse<AgeCoefficient>> getAgeCoefficient(@Parameter(description = "ID du coefficient") @PathVariable Long id) {
        AgeCoefficient coef = ageCoefficientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AgeCoefficient", "id", id));
        return ResponseEntity.ok(ApiResponse.success(coef));
    }

    @Operation(summary = "Creer un coefficient", description = "Cree un nouveau coefficient d'age")
    @PostMapping("/age-coefficients")
    public ResponseEntity<ApiResponse<AgeCoefficient>> createAgeCoefficient(@Valid @RequestBody AgeCoefficient coef) {
        coef.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(ageCoefficientRepository.save(coef), "Coefficient created."));
    }

    @Operation(summary = "Modifier un coefficient", description = "Met a jour un coefficient d'age existant")
    @PutMapping("/age-coefficients/{id}")
    public ResponseEntity<ApiResponse<AgeCoefficient>> updateAgeCoefficient(
            @Parameter(description = "ID du coefficient") @PathVariable Long id,
            @Valid @RequestBody AgeCoefficient coef) {

        if (!ageCoefficientRepository.existsById(id)) {
            throw new ResourceNotFoundException("AgeCoefficient", "id", id);
        }
        coef.setId(id);
        return ResponseEntity.ok(ApiResponse.success(ageCoefficientRepository.save(coef), "Coefficient updated."));
    }

    @Operation(summary = "Supprimer un coefficient", description = "Supprime un coefficient d'age")
    @DeleteMapping("/age-coefficients/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteAgeCoefficient(@Parameter(description = "ID du coefficient") @PathVariable Long id) {
        if (!ageCoefficientRepository.existsById(id)) {
            throw new ResourceNotFoundException("AgeCoefficient", "id", id);
        }
        ageCoefficientRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Coefficient deleted."));
    }

    // ==================== Tax Exemptions ====================

    @Operation(summary = "Liste des exemptions", description = "Liste toutes les exemptions de taxes avec filtres optionnels")
    @GetMapping("/tax-exemptions")
    public ResponseEntity<ApiResponse<List<TaxExemption>>> getAllTaxExemptions(
            @Parameter(description = "Region") @RequestParam(required = false) Region region,
            @Parameter(description = "Type de taxe") @RequestParam(required = false) TaxType taxType) {

        List<TaxExemption> exemptions = (region != null && taxType != null)
                ? taxExemptionRepository.findByRegionAndTaxType(region, taxType)
                : taxExemptionRepository.findAll();
        return ResponseEntity.ok(ApiResponse.success(exemptions));
    }

    @Operation(summary = "Detail d'une exemption", description = "Recupere une exemption de taxe par son ID")
    @GetMapping("/tax-exemptions/{id}")
    public ResponseEntity<ApiResponse<TaxExemption>> getTaxExemption(@Parameter(description = "ID de l'exemption") @PathVariable Long id) {
        TaxExemption exemption = taxExemptionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TaxExemption", "id", id));
        return ResponseEntity.ok(ApiResponse.success(exemption));
    }

    @Operation(summary = "Creer une exemption", description = "Cree une nouvelle exemption de taxe")
    @PostMapping("/tax-exemptions")
    public ResponseEntity<ApiResponse<TaxExemption>> createTaxExemption(@Valid @RequestBody TaxExemption exemption) {
        exemption.setId(null);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(taxExemptionRepository.save(exemption), "Exemption created."));
    }

    @Operation(summary = "Modifier une exemption", description = "Met a jour une exemption de taxe existante")
    @PutMapping("/tax-exemptions/{id}")
    public ResponseEntity<ApiResponse<TaxExemption>> updateTaxExemption(
            @Parameter(description = "ID de l'exemption") @PathVariable Long id,
            @Valid @RequestBody TaxExemption exemption) {

        if (!taxExemptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("TaxExemption", "id", id);
        }
        exemption.setId(id);
        return ResponseEntity.ok(ApiResponse.success(taxExemptionRepository.save(exemption), "Exemption updated."));
    }

    @Operation(summary = "Supprimer une exemption", description = "Supprime une exemption de taxe")
    @DeleteMapping("/tax-exemptions/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTaxExemption(@Parameter(description = "ID de l'exemption") @PathVariable Long id) {
        if (!taxExemptionRepository.existsById(id)) {
            throw new ResourceNotFoundException("TaxExemption", "id", id);
        }
        taxExemptionRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Exemption deleted."));
    }
}
