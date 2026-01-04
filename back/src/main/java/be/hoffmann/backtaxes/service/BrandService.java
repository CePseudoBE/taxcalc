package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.Brand;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.repository.BrandRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des marques de vehicules.
 */
@Service
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    /**
     * Recupere toutes les marques.
     */
    public List<Brand> findAll() {
        return brandRepository.findAll();
    }

    /**
     * Recupere une marque par son ID.
     * @throws ResourceNotFoundException si la marque n'existe pas
     */
    public Brand findById(Long id) {
        return brandRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Brand", "id", id));
    }

    /**
     * Recupere une marque par son nom.
     */
    public Optional<Brand> findByName(String name) {
        return brandRepository.findByName(name);
    }

    /**
     * Verifie si une marque existe.
     */
    public boolean existsById(Long id) {
        return brandRepository.existsById(id);
    }
}
