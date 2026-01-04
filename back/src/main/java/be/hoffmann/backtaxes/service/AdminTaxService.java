package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.request.IndexationRequest;
import be.hoffmann.backtaxes.dto.request.TaxBracketRequest;
import be.hoffmann.backtaxes.dto.request.TaxParameterRequest;
import be.hoffmann.backtaxes.dto.response.IndexationResponse;
import be.hoffmann.backtaxes.dto.response.TaxBracketResponse;
import be.hoffmann.backtaxes.dto.response.TaxParameterResponse;
import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.TaxParameter;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.repository.TaxBracketRepository;
import be.hoffmann.backtaxes.repository.TaxParameterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Service d'administration des taxes.
 * Permet de gerer les baremes et parametres de taxes,
 * et d'appliquer des indexations en masse.
 */
@Service
@Transactional
public class AdminTaxService {

    private final TaxBracketRepository taxBracketRepository;
    private final TaxParameterRepository taxParameterRepository;

    public AdminTaxService(
            TaxBracketRepository taxBracketRepository,
            TaxParameterRepository taxParameterRepository) {
        this.taxBracketRepository = taxBracketRepository;
        this.taxParameterRepository = taxParameterRepository;
    }

    // ==================== TAX BRACKETS ====================

    public List<TaxBracketResponse> getAllBrackets() {
        return taxBracketRepository.findAll().stream()
                .map(TaxBracketResponse::from)
                .toList();
    }

    public List<TaxBracketResponse> getBracketsByRegionAndType(Region region, TaxType taxType) {
        return taxBracketRepository.findByRegionAndTaxType(region, taxType).stream()
                .map(TaxBracketResponse::from)
                .toList();
    }

    public TaxBracketResponse getBracketById(Long id) {
        return taxBracketRepository.findById(id)
                .map(TaxBracketResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Bracket not found: " + id));
    }

    public TaxBracketResponse createBracket(TaxBracketRequest request) {
        TaxBracket bracket = new TaxBracket();
        mapRequestToBracket(request, bracket);
        return TaxBracketResponse.from(taxBracketRepository.save(bracket));
    }

    public TaxBracketResponse updateBracket(Long id, TaxBracketRequest request) {
        TaxBracket bracket = taxBracketRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bracket not found: " + id));
        mapRequestToBracket(request, bracket);
        return TaxBracketResponse.from(taxBracketRepository.save(bracket));
    }

    public void deleteBracket(Long id) {
        if (!taxBracketRepository.existsById(id)) {
            throw new IllegalArgumentException("Bracket not found: " + id);
        }
        taxBracketRepository.deleteById(id);
    }

    private void mapRequestToBracket(TaxBracketRequest request, TaxBracket bracket) {
        bracket.setRegion(request.region());
        bracket.setTaxType(request.taxType());
        bracket.setBracketKey(request.bracketKey());
        bracket.setMinValue(request.minValue());
        bracket.setMaxValue(request.maxValue());
        bracket.setAmount(request.amount());
        bracket.setValidFrom(request.validFrom());
        bracket.setValidTo(request.validTo());
    }

    // ==================== TAX PARAMETERS ====================

    public List<TaxParameterResponse> getAllParameters() {
        return taxParameterRepository.findAll().stream()
                .map(TaxParameterResponse::from)
                .toList();
    }

    public List<TaxParameterResponse> getParametersByRegionAndType(Region region, TaxType taxType) {
        return taxParameterRepository.findByRegionAndTaxType(region, taxType).stream()
                .map(TaxParameterResponse::from)
                .toList();
    }

    public TaxParameterResponse getParameterById(Long id) {
        return taxParameterRepository.findById(id)
                .map(TaxParameterResponse::from)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found: " + id));
    }

    public TaxParameterResponse createParameter(TaxParameterRequest request) {
        TaxParameter param = new TaxParameter();
        mapRequestToParameter(request, param);
        return TaxParameterResponse.from(taxParameterRepository.save(param));
    }

    public TaxParameterResponse updateParameter(Long id, TaxParameterRequest request) {
        TaxParameter param = taxParameterRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Parameter not found: " + id));
        mapRequestToParameter(request, param);
        return TaxParameterResponse.from(taxParameterRepository.save(param));
    }

    public void deleteParameter(Long id) {
        if (!taxParameterRepository.existsById(id)) {
            throw new IllegalArgumentException("Parameter not found: " + id);
        }
        taxParameterRepository.deleteById(id);
    }

    private void mapRequestToParameter(TaxParameterRequest request, TaxParameter param) {
        param.setRegion(request.region());
        param.setTaxType(request.taxType());
        param.setParamKey(request.paramKey());
        param.setParamValue(request.paramValue());
        param.setValidFrom(request.validFrom());
        param.setValidTo(request.validTo());
    }

    // ==================== INDEXATION ====================

    /**
     * Applique une indexation en masse.
     *
     * Cette methode:
     * 1. Ferme les anciens baremes/parametres (validTo = effectiveDate - 1 jour)
     * 2. Cree de nouveaux baremes/parametres avec les montants indexes
     *
     * @param request les parametres d'indexation
     * @return le resultat de l'indexation
     */
    public IndexationResponse applyIndexation(IndexationRequest request) {
        int bracketsUpdated = 0;
        int parametersUpdated = 0;

        LocalDate previousDay = request.effectiveDate().minusDays(1);

        // Indexer les baremes
        if (request.includeBrackets()) {
            bracketsUpdated = indexBrackets(request, previousDay);
        }

        // Indexer les parametres
        if (request.includeParameters()) {
            parametersUpdated = indexParameters(request, previousDay);
        }

        String message = String.format(
                "Indexation de %.2f%% appliquee a partir du %s",
                (request.indexationRate().subtract(BigDecimal.ONE)).multiply(BigDecimal.valueOf(100)),
                request.effectiveDate()
        );

        return new IndexationResponse(
                bracketsUpdated,
                parametersUpdated,
                request.indexationRate(),
                request.effectiveDate(),
                message
        );
    }

    private int indexBrackets(IndexationRequest request, LocalDate previousDay) {
        List<TaxBracket> brackets = taxBracketRepository.findValidBracketsWithFilters(
                previousDay, request.region(), request.taxType());

        List<TaxBracket> newBrackets = new java.util.ArrayList<>();

        for (TaxBracket oldBracket : brackets) {
            // Fermer l'ancien bareme
            oldBracket.setValidTo(previousDay);

            // Creer le nouveau bareme indexe
            TaxBracket newBracket = new TaxBracket();
            newBracket.setRegion(oldBracket.getRegion());
            newBracket.setTaxType(oldBracket.getTaxType());
            newBracket.setBracketKey(oldBracket.getBracketKey());
            newBracket.setMinValue(oldBracket.getMinValue());
            newBracket.setMaxValue(oldBracket.getMaxValue());
            newBracket.setValidFrom(request.effectiveDate());
            newBracket.setValidTo(null);

            // Appliquer l'indexation
            BigDecimal indexed = oldBracket.getAmount()
                    .multiply(request.indexationRate())
                    .setScale(2, RoundingMode.HALF_UP);
            newBracket.setAmount(indexed);

            newBrackets.add(newBracket);
        }

        // Batch save: anciennes + nouvelles
        taxBracketRepository.saveAll(brackets);
        taxBracketRepository.saveAll(newBrackets);

        return newBrackets.size();
    }

    private int indexParameters(IndexationRequest request, LocalDate previousDay) {
        List<TaxParameter> params = taxParameterRepository.findValidParametersWithFilters(
                previousDay, request.region(), request.taxType());

        List<TaxParameter> newParams = new java.util.ArrayList<>();

        for (TaxParameter oldParam : params) {
            // Fermer l'ancien parametre
            oldParam.setValidTo(previousDay);

            // Creer le nouveau parametre indexe
            TaxParameter newParam = new TaxParameter();
            newParam.setRegion(oldParam.getRegion());
            newParam.setTaxType(oldParam.getTaxType());
            newParam.setParamKey(oldParam.getParamKey());
            newParam.setValidFrom(request.effectiveDate());
            newParam.setValidTo(null);

            // Appliquer l'indexation (sauf pour certains parametres non-monetaires)
            if (shouldIndexParameter(oldParam.getParamKey())) {
                BigDecimal indexed = oldParam.getParamValue()
                        .multiply(request.indexationRate())
                        .setScale(4, RoundingMode.HALF_UP);
                newParam.setParamValue(indexed);
            } else {
                newParam.setParamValue(oldParam.getParamValue());
            }

            newParams.add(newParam);
        }

        // Batch save: anciennes + nouvelles
        taxParameterRepository.saveAll(params);
        taxParameterRepository.saveAll(newParams);

        return newParams.size();
    }

    /**
     * Determine si un parametre doit etre indexe.
     * Les coefficients et ratios ne sont pas indexes,
     * seuls les montants monetaires le sont.
     */
    private boolean shouldIndexParameter(String paramKey) {
        // Parametres qui ne doivent PAS etre indexes (coefficients, ratios, etc.)
        return switch (paramKey) {
            case "co2_reference_wltp",
                 "mma_reference",
                 "co2_correction_factor",
                 "co2_correction_base",
                 "energy_thermal",
                 "energy_hybrid",
                 "energy_plugin_hybrid" -> false;
            default -> true;  // Tous les autres (montants) sont indexes
        };
    }
}
