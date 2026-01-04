package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.request.SavedSearchRequest;
import be.hoffmann.backtaxes.dto.response.SavedSearchResponse;
import be.hoffmann.backtaxes.entity.SavedSearch;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.entity.Variant;
import be.hoffmann.backtaxes.entity.VehicleSubmission;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.repository.SavedSearchRepository;
import be.hoffmann.backtaxes.repository.VehicleSubmissionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Service pour la gestion des recherches sauvegardees.
 */
@Service
@Transactional(readOnly = true)
public class SavedSearchService {

    private final SavedSearchRepository savedSearchRepository;
    private final VariantService variantService;
    private final VehicleSubmissionRepository submissionRepository;

    public SavedSearchService(
            SavedSearchRepository savedSearchRepository,
            VariantService variantService,
            VehicleSubmissionRepository submissionRepository) {
        this.savedSearchRepository = savedSearchRepository;
        this.variantService = variantService;
        this.submissionRepository = submissionRepository;
    }

    /**
     * Sauvegarde une recherche.
     */
    @Transactional
    public SavedSearch save(SavedSearchRequest request, User user) {
        if (!request.hasValidVehicleReference()) {
            throw new ValidationException("Either variantId or submissionId must be provided, but not both");
        }

        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setUser(user);
        savedSearch.setRegion(request.getRegion());
        savedSearch.setFirstRegistrationDate(request.getFirstRegistrationDate());
        savedSearch.setLabel(request.getLabel());

        if (request.getVariantId() != null) {
            Variant variant = variantService.findById(request.getVariantId());
            savedSearch.setVariant(variant);
        } else {
            VehicleSubmission submission = submissionRepository.findById(request.getSubmissionId())
                    .orElseThrow(() -> new ResourceNotFoundException("VehicleSubmission", "id", request.getSubmissionId()));
            savedSearch.setSubmission(submission);
        }

        return savedSearchRepository.save(savedSearch);
    }

    /**
     * Liste les recherches sauvegardees d'un utilisateur.
     * Utilise JOIN FETCH pour eviter le probleme N+1.
     */
    public List<SavedSearch> findByUser(Long userId) {
        return savedSearchRepository.findByUserIdWithDetails(userId);
    }

    /**
     * Trouve une recherche sauvegardee par ID.
     * Utilise JOIN FETCH pour charger toutes les relations.
     */
    public SavedSearch findById(Long id) {
        return savedSearchRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("SavedSearch", "id", id));
    }

    /**
     * Supprime une recherche sauvegardee.
     */
    @Transactional
    public void delete(Long id, Long userId) {
        SavedSearch savedSearch = findById(id);
        if (!savedSearch.getUser().getId().equals(userId)) {
            throw new ValidationException("You can only delete your own saved searches");
        }
        savedSearchRepository.delete(savedSearch);
    }

    /**
     * Convertit en DTO response.
     */
    public SavedSearchResponse toResponse(SavedSearch savedSearch) {
        SavedSearchResponse.VehicleSummary summary = createVehicleSummary(savedSearch);

        return new SavedSearchResponse(
                savedSearch.getId(),
                savedSearch.getLabel(),
                savedSearch.getRegion(),
                savedSearch.getFirstRegistrationDate(),
                summary,
                savedSearch.getCreatedAt()
        );
    }

    private SavedSearchResponse.VehicleSummary createVehicleSummary(SavedSearch savedSearch) {
        if (savedSearch.getVariant() != null) {
            Variant v = savedSearch.getVariant();
            return new SavedSearchResponse.VehicleSummary(
                    v.getId(),
                    "variant",
                    v.getModel().getBrand().getName(),
                    v.getModel().getName(),
                    v.getName(),
                    v.getPowerKw(),
                    v.getFuel() != null ? v.getFuel().name() : null
            );
        } else if (savedSearch.getSubmission() != null) {
            VehicleSubmission s = savedSearch.getSubmission();
            return new SavedSearchResponse.VehicleSummary(
                    s.getId(),
                    "submission",
                    s.getBrandName(),
                    s.getModelName(),
                    s.getVariantName(),
                    s.getPowerKw(),
                    s.getFuel() != null ? s.getFuel().name() : null
            );
        }
        return null;
    }

    /**
     * Convertit une liste en DTOs.
     */
    public List<SavedSearchResponse> toResponseList(List<SavedSearch> savedSearches) {
        return savedSearches.stream()
                .map(this::toResponse)
                .toList();
    }
}
