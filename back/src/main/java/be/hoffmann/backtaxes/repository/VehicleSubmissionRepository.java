package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.VehicleSubmission;
import be.hoffmann.backtaxes.entity.enums.SubmissionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VehicleSubmissionRepository extends JpaRepository<VehicleSubmission, Long> {

    List<VehicleSubmission> findBySubmitterId(Long submitterId);

    /**
     * Charge les soumissions d'un utilisateur avec toutes les relations.
     */
    @Query("SELECT s FROM VehicleSubmission s " +
           "LEFT JOIN FETCH s.submitter " +
           "LEFT JOIN FETCH s.reviewer " +
           "LEFT JOIN FETCH s.createdVariant " +
           "WHERE s.submitter.id = :submitterId")
    List<VehicleSubmission> findBySubmitterIdWithDetails(@Param("submitterId") Long submitterId);

    List<VehicleSubmission> findByStatus(SubmissionStatus status);

    /**
     * Charge les soumissions par statut avec toutes les relations.
     */
    @Query("SELECT s FROM VehicleSubmission s " +
           "LEFT JOIN FETCH s.submitter " +
           "LEFT JOIN FETCH s.reviewer " +
           "LEFT JOIN FETCH s.createdVariant " +
           "WHERE s.status = :status")
    List<VehicleSubmission> findByStatusWithDetails(@Param("status") SubmissionStatus status);

    List<VehicleSubmission> findByReviewerId(Long reviewerId);

    /**
     * Charge les soumissions d'un reviewer avec toutes les relations.
     */
    @Query("SELECT s FROM VehicleSubmission s " +
           "LEFT JOIN FETCH s.submitter " +
           "LEFT JOIN FETCH s.reviewer " +
           "LEFT JOIN FETCH s.createdVariant " +
           "WHERE s.reviewer.id = :reviewerId")
    List<VehicleSubmission> findByReviewerIdWithDetails(@Param("reviewerId") Long reviewerId);

    /**
     * Charge une soumission par ID avec toutes les relations.
     */
    @Query("SELECT s FROM VehicleSubmission s " +
           "LEFT JOIN FETCH s.submitter " +
           "LEFT JOIN FETCH s.reviewer " +
           "LEFT JOIN FETCH s.createdVariant " +
           "WHERE s.id = :id")
    Optional<VehicleSubmission> findByIdWithDetails(@Param("id") Long id);

    long countByStatus(SubmissionStatus status);

    /**
     * Charge les soumissions par statut avec pagination.
     */
    @Query(value = "SELECT s FROM VehicleSubmission s " +
           "LEFT JOIN FETCH s.submitter " +
           "LEFT JOIN FETCH s.reviewer " +
           "LEFT JOIN FETCH s.createdVariant " +
           "WHERE s.status = :status",
           countQuery = "SELECT COUNT(s) FROM VehicleSubmission s WHERE s.status = :status")
    Page<VehicleSubmission> findByStatusWithDetailsPaged(@Param("status") SubmissionStatus status, Pageable pageable);
}
