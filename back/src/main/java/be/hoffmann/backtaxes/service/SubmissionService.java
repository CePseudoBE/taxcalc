package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.mapper.SubmissionMapper;
import be.hoffmann.backtaxes.dto.request.VehicleSubmissionRequest;
import be.hoffmann.backtaxes.entity.*;
import be.hoffmann.backtaxes.entity.enums.SubmissionStatus;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.repository.BrandRepository;
import be.hoffmann.backtaxes.repository.ModelRepository;
import be.hoffmann.backtaxes.repository.VariantRepository;
import be.hoffmann.backtaxes.repository.VehicleSubmissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Service pour la gestion des soumissions de vehicules.
 */
@Service
@Transactional(readOnly = true)
public class SubmissionService {

    private final VehicleSubmissionRepository submissionRepository;
    private final BrandRepository brandRepository;
    private final ModelRepository modelRepository;
    private final VariantRepository variantRepository;

    public SubmissionService(
            VehicleSubmissionRepository submissionRepository,
            BrandRepository brandRepository,
            ModelRepository modelRepository,
            VariantRepository variantRepository) {
        this.submissionRepository = submissionRepository;
        this.brandRepository = brandRepository;
        this.modelRepository = modelRepository;
        this.variantRepository = variantRepository;
    }

    /**
     * Cree une nouvelle soumission.
     */
    @Transactional
    public VehicleSubmission create(VehicleSubmissionRequest request, User submitter) {
        VehicleSubmission submission = SubmissionMapper.toEntity(request, submitter);
        return submissionRepository.save(submission);
    }

    /**
     * Trouve une soumission par ID avec toutes les relations.
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public VehicleSubmission findById(Long id) {
        return submissionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("VehicleSubmission", "id", id));
    }

    /**
     * Liste les soumissions d'un utilisateur.
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public List<VehicleSubmission> findByUser(Long userId) {
        return submissionRepository.findBySubmitterIdWithDetails(userId);
    }

    /**
     * Liste les soumissions en attente de moderation.
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public List<VehicleSubmission> findPending() {
        return submissionRepository.findByStatusWithDetails(SubmissionStatus.pending);
    }

    /**
     * Liste les soumissions par statut.
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public List<VehicleSubmission> findByStatus(SubmissionStatus status) {
        return submissionRepository.findByStatusWithDetails(status);
    }

    /**
     * Liste les soumissions par statut avec pagination.
     * Utilise JOIN FETCH pour eviter N+1.
     */
    public Page<VehicleSubmission> findByStatus(SubmissionStatus status, Pageable pageable) {
        return submissionRepository.findByStatusWithDetailsPaged(status, pageable);
    }

    /**
     * Approuve une soumission et cree la variante correspondante.
     */
    @Transactional
    public VehicleSubmission approve(Long submissionId, User reviewer) {
        VehicleSubmission submission = findById(submissionId);

        if (submission.getStatus() != SubmissionStatus.pending) {
            throw new ValidationException("Submission is not pending");
        }

        // Creer ou trouver la marque
        Brand brand = brandRepository.findByName(submission.getBrandName())
                .orElseGet(() -> {
                    Brand newBrand = new Brand();
                    newBrand.setName(submission.getBrandName());
                    return brandRepository.save(newBrand);
                });

        // Creer ou trouver le modele
        Model model = modelRepository.findByBrandIdAndName(brand.getId(), submission.getModelName())
                .orElseGet(() -> {
                    Model newModel = new Model();
                    newModel.setBrand(brand);
                    newModel.setName(submission.getModelName());
                    return modelRepository.save(newModel);
                });

        // Creer la variante
        Variant variant = new Variant();
        variant.setModel(model);
        variant.setName(submission.getVariantName());
        variant.setYearStart(submission.getYearStart());
        variant.setYearEnd(submission.getYearEnd());
        variant.setPowerKw(submission.getPowerKw());
        variant.setFiscalHp(submission.getFiscalHp());
        variant.setFuel(submission.getFuel());
        variant.setEuroNorm(submission.getEuroNorm());
        variant.setCo2Wltp(submission.getCo2Wltp());
        variant.setCo2Nedc(submission.getCo2Nedc());
        variant.setDisplacementCc(submission.getDisplacementCc());
        variant.setMmaKg(submission.getMmaKg());
        variant.setHasParticleFilter(submission.getHasParticleFilter());
        variant = variantRepository.save(variant);

        // Mettre a jour la soumission
        submission.setStatus(SubmissionStatus.approved);
        submission.setReviewer(reviewer);
        submission.setReviewedAt(Instant.now());
        submission.setCreatedVariant(variant);

        return submissionRepository.save(submission);
    }

    /**
     * Rejette une soumission.
     */
    @Transactional
    public VehicleSubmission reject(Long submissionId, User reviewer, String feedback) {
        VehicleSubmission submission = findById(submissionId);

        if (submission.getStatus() != SubmissionStatus.pending) {
            throw new ValidationException("Submission is not pending");
        }

        submission.setStatus(SubmissionStatus.rejected);
        submission.setReviewer(reviewer);
        submission.setReviewedAt(Instant.now());
        submission.setFeedback(feedback);

        return submissionRepository.save(submission);
    }
}
