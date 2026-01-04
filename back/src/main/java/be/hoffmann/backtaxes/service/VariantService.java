package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.Variant;
import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.repository.VariantRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour la gestion des variantes de vehicules.
 */
@Service
@Transactional(readOnly = true)
public class VariantService {

    private final VariantRepository variantRepository;
    private final ModelService modelService;

    public VariantService(VariantRepository variantRepository, ModelService modelService) {
        this.variantRepository = variantRepository;
        this.modelService = modelService;
    }

    /**
     * Recupere toutes les variantes d'un modele.
     * Utilise JOIN FETCH pour eviter N+1.
     * @throws ResourceNotFoundException si le modele n'existe pas
     */
    public List<Variant> findByModelId(Long modelId) {
        // Verifier que le modele existe
        modelService.findById(modelId);
        return variantRepository.findByModelIdWithDetails(modelId);
    }

    /**
     * Recupere une variante par son ID.
     * @throws ResourceNotFoundException si la variante n'existe pas
     */
    public Variant findById(Long id) {
        return variantRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", id));
    }

    /**
     * Recupere une variante avec son model et sa brand en une seule requete.
     * Utiliser cette methode pour eviter le probleme N+1 lors de l'affichage des details.
     * @throws ResourceNotFoundException si la variante n'existe pas
     */
    public Variant findByIdWithDetails(Long id) {
        return variantRepository.findByIdWithModelAndBrand(id)
                .orElseThrow(() -> new ResourceNotFoundException("Variant", "id", id));
    }

    /**
     * Recherche de variantes avec filtres multiples.
     */
    public List<Variant> search(VariantSearchCriteria criteria) {
        return variantRepository.search(
                criteria.getBrandId(),
                criteria.getModelId(),
                criteria.getFuelTypes(),
                criteria.getEuroNorms(),
                criteria.getMinPower(),
                criteria.getMaxPower(),
                criteria.getMinYear(),
                criteria.getMaxYear(),
                criteria.getMaxCo2()
        );
    }

    /**
     * Recherche paginee de variantes avec filtres multiples.
     * Utiliser pour les resultats potentiellement volumineux.
     */
    public Page<Variant> searchPaginated(VariantSearchCriteria criteria, Pageable pageable) {
        return variantRepository.searchPaginated(
                criteria.getBrandId(),
                criteria.getModelId(),
                criteria.getFuelTypes(),
                criteria.getEuroNorms(),
                criteria.getMinPower(),
                criteria.getMaxPower(),
                criteria.getMinYear(),
                criteria.getMaxYear(),
                criteria.getMaxCo2(),
                pageable
        );
    }

    /**
     * Recherche par type de carburant.
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public List<Variant> findByFuelType(FuelType fuelType) {
        return variantRepository.findByFuelWithDetails(fuelType);
    }

    /**
     * Recherche par norme Euro.
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public List<Variant> findByEuroNorm(EuroNorm euroNorm) {
        return variantRepository.findByEuroNormWithDetails(euroNorm);
    }

    /**
     * Verifie si une variante existe.
     */
    public boolean existsById(Long id) {
        return variantRepository.existsById(id);
    }

    /**
     * Criteres de recherche pour les variantes.
     */
    public static class VariantSearchCriteria {
        private Long brandId;
        private Long modelId;
        private List<FuelType> fuelTypes;
        private List<EuroNorm> euroNorms;
        private Integer minPower;
        private Integer maxPower;
        private Integer minYear;
        private Integer maxYear;
        private Integer maxCo2;

        public Long getBrandId() {
            return brandId;
        }

        public void setBrandId(Long brandId) {
            this.brandId = brandId;
        }

        public Long getModelId() {
            return modelId;
        }

        public void setModelId(Long modelId) {
            this.modelId = modelId;
        }

        public List<FuelType> getFuelTypes() {
            return fuelTypes;
        }

        public void setFuelTypes(List<FuelType> fuelTypes) {
            this.fuelTypes = fuelTypes;
        }

        public List<EuroNorm> getEuroNorms() {
            return euroNorms;
        }

        public void setEuroNorms(List<EuroNorm> euroNorms) {
            this.euroNorms = euroNorms;
        }

        public Integer getMinPower() {
            return minPower;
        }

        public void setMinPower(Integer minPower) {
            this.minPower = minPower;
        }

        public Integer getMaxPower() {
            return maxPower;
        }

        public void setMaxPower(Integer maxPower) {
            this.maxPower = maxPower;
        }

        public Integer getMinYear() {
            return minYear;
        }

        public void setMinYear(Integer minYear) {
            this.minYear = minYear;
        }

        public Integer getMaxYear() {
            return maxYear;
        }

        public void setMaxYear(Integer maxYear) {
            this.maxYear = maxYear;
        }

        public Integer getMaxCo2() {
            return maxCo2;
        }

        public void setMaxCo2(Integer maxCo2) {
            this.maxCo2 = maxCo2;
        }
    }
}
