package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.Model;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.repository.ModelRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour la gestion des modeles de vehicules.
 */
@Service
@Transactional(readOnly = true)
public class ModelService {

    private final ModelRepository modelRepository;
    private final BrandService brandService;

    public ModelService(ModelRepository modelRepository, BrandService brandService) {
        this.modelRepository = modelRepository;
        this.brandService = brandService;
    }

    /**
     * Recupere tous les modeles d'une marque.
     * Utilise JOIN FETCH pour eviter le probleme N+1.
     * @throws ResourceNotFoundException si la marque n'existe pas
     */
    public List<Model> findByBrandId(Long brandId) {
        // Verifier que la marque existe
        brandService.findById(brandId);
        // Utilise la version avec JOIN FETCH pour eviter N+1
        return modelRepository.findByBrandIdWithBrand(brandId);
    }

    /**
     * Recupere un modele par son ID avec sa brand pre-chargee.
     * Utilise JOIN FETCH pour eviter N+1.
     * @throws ResourceNotFoundException si le modele n'existe pas
     */
    public Model findById(Long id) {
        return modelRepository.findByIdWithBrand(id)
                .orElseThrow(() -> new ResourceNotFoundException("Model", "id", id));
    }

    /**
     * Recherche des modeles par nom (partiel).
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public List<Model> searchByName(String keyword) {
        return modelRepository.searchByNameWithBrand(keyword);
    }

    /**
     * Recherche des modeles par marque et nom.
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public List<Model> searchByBrandAndName(Long brandId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return findByBrandId(brandId);
        }
        return modelRepository.searchByBrandIdAndNameWithBrand(brandId, keyword);
    }

    /**
     * Verifie si un modele existe.
     */
    public boolean existsById(Long id) {
        return modelRepository.existsById(id);
    }
}
