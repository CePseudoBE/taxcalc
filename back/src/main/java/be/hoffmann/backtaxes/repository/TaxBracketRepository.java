package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxBracketRepository extends JpaRepository<TaxBracket, Long> {

    List<TaxBracket> findByRegionAndTaxType(Region region, TaxType taxType);

    List<TaxBracket> findByRegionAndTaxTypeAndBracketKey(Region region, TaxType taxType, String bracketKey);

    @Query("SELECT tb FROM TaxBracket tb WHERE tb.region = :region AND tb.taxType = :taxType " +
           "AND tb.bracketKey = :bracketKey AND tb.validFrom <= :date " +
           "AND (tb.validTo IS NULL OR tb.validTo >= :date)")
    List<TaxBracket> findValidBrackets(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("bracketKey") String bracketKey,
            @Param("date") LocalDate date);

    @Query("SELECT tb FROM TaxBracket tb WHERE tb.region = :region AND tb.taxType = :taxType " +
           "AND tb.bracketKey = :bracketKey AND :value >= tb.minValue " +
           "AND (:value <= tb.maxValue OR tb.maxValue IS NULL) " +
           "AND tb.validFrom <= :date AND (tb.validTo IS NULL OR tb.validTo >= :date)")
    Optional<TaxBracket> findMatchingBracket(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("bracketKey") String bracketKey,
            @Param("value") int value,
            @Param("date") LocalDate date);

    /**
     * Trouve les baremes valides a une date donnee avec filtres optionnels.
     */
    @Query("SELECT tb FROM TaxBracket tb WHERE tb.validFrom <= :date " +
           "AND (tb.validTo IS NULL OR tb.validTo >= :date) " +
           "AND (:region IS NULL OR tb.region = :region) " +
           "AND (:taxType IS NULL OR tb.taxType = :taxType)")
    List<TaxBracket> findValidBracketsWithFilters(
            @Param("date") LocalDate date,
            @Param("region") Region region,
            @Param("taxType") TaxType taxType);

    /**
     * Recupere les tranches avec pagination et filtres optionnels.
     */
    @Query(value = "SELECT tb FROM TaxBracket tb " +
           "WHERE (:region IS NULL OR tb.region = :region) " +
           "AND (:taxType IS NULL OR tb.taxType = :taxType)",
           countQuery = "SELECT COUNT(tb) FROM TaxBracket tb " +
           "WHERE (:region IS NULL OR tb.region = :region) " +
           "AND (:taxType IS NULL OR tb.taxType = :taxType)")
    Page<TaxBracket> findByFilters(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            Pageable pageable);
}
