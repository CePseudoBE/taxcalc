package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.TaxCalculation;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Repository
public interface TaxCalculationRepository extends JpaRepository<TaxCalculation, Long> {

    List<TaxCalculation> findByVariantId(Long variantId);

    List<TaxCalculation> findByRegion(Region region);

    List<TaxCalculation> findByTaxType(TaxType taxType);

    List<TaxCalculation> findByCreatedAtBetween(Instant start, Instant end);

    @Query("SELECT AVG(tc.calculatedAmount) FROM TaxCalculation tc WHERE tc.createdAt BETWEEN :start AND :end")
    BigDecimal avgAmountBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT AVG(tc.calculatedAmount) FROM TaxCalculation tc WHERE tc.region = :region AND tc.taxType = :taxType AND tc.createdAt BETWEEN :start AND :end")
    BigDecimal avgAmountByRegionAndTypeBetween(@Param("region") Region region, @Param("taxType") TaxType taxType, @Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT MAX(tc.calculatedAmount) FROM TaxCalculation tc WHERE tc.createdAt BETWEEN :start AND :end")
    BigDecimal maxAmountBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT MIN(tc.calculatedAmount) FROM TaxCalculation tc WHERE tc.createdAt BETWEEN :start AND :end AND tc.isExempt = false")
    BigDecimal minNonExemptAmountBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT tc FROM TaxCalculation tc WHERE tc.createdAt BETWEEN :start AND :end ORDER BY tc.calculatedAmount DESC")
    List<TaxCalculation> findHighestTaxesBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT tc.variant.id, AVG(tc.calculatedAmount), COUNT(tc) FROM TaxCalculation tc WHERE tc.createdAt BETWEEN :start AND :end AND tc.variant IS NOT NULL GROUP BY tc.variant.id ORDER BY COUNT(tc) DESC")
    List<Object[]> findMostCalculatedVariantsBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT tc.region, AVG(tc.calculatedAmount) FROM TaxCalculation tc WHERE tc.taxType = :taxType AND tc.createdAt BETWEEN :start AND :end GROUP BY tc.region")
    List<Object[]> avgAmountByRegionAndType(@Param("taxType") TaxType taxType, @Param("start") Instant start, @Param("end") Instant end);

    Long countByIsExemptAndCreatedAtBetween(Boolean isExempt, Instant start, Instant end);

    void deleteByCreatedAtBefore(Instant before);
}
