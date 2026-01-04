package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.SavedSearch;
import be.hoffmann.backtaxes.entity.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SavedSearchRepository extends JpaRepository<SavedSearch, Long> {

    /**
     * Charge les recherches sauvegardees avec toutes les relations en une seule requete.
     * Evite le probleme N+1 lors de l'acces aux variants/submissions.
     */
    @Query("SELECT DISTINCT ss FROM SavedSearch ss " +
           "LEFT JOIN FETCH ss.variant v " +
           "LEFT JOIN FETCH v.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH ss.submission " +
           "WHERE ss.user.id = :userId")
    List<SavedSearch> findByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT DISTINCT ss FROM SavedSearch ss " +
           "LEFT JOIN FETCH ss.variant v " +
           "LEFT JOIN FETCH v.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH ss.submission " +
           "WHERE ss.user.id = :userId AND ss.region = :region")
    List<SavedSearch> findByUserIdAndRegionWithDetails(@Param("userId") Long userId, @Param("region") Region region);

    @Query("SELECT ss FROM SavedSearch ss " +
           "LEFT JOIN FETCH ss.user " +
           "LEFT JOIN FETCH ss.variant v " +
           "LEFT JOIN FETCH v.model m " +
           "LEFT JOIN FETCH m.brand " +
           "LEFT JOIN FETCH ss.submission " +
           "WHERE ss.id = :id")
    Optional<SavedSearch> findByIdWithDetails(@Param("id") Long id);

    List<SavedSearch> findByUserId(Long userId);

    List<SavedSearch> findByUserIdAndRegion(Long userId, Region region);

    List<SavedSearch> findByVariantId(Long variantId);

    List<SavedSearch> findBySubmissionId(Long submissionId);
}
