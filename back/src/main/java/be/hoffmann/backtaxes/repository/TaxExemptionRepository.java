package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.TaxExemption;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaxExemptionRepository extends JpaRepository<TaxExemption, Long> {

    List<TaxExemption> findByRegionAndTaxType(Region region, TaxType taxType);

    @Query("SELECT te FROM TaxExemption te WHERE te.region = :region AND te.taxType = :taxType " +
           "AND te.conditionKey = :conditionKey AND te.validFrom <= :date " +
           "AND (te.validTo IS NULL OR te.validTo >= :date)")
    Optional<TaxExemption> findValidExemption(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("conditionKey") String conditionKey,
            @Param("date") LocalDate date);

    @Query("SELECT COUNT(te) > 0 FROM TaxExemption te WHERE te.region = :region AND te.taxType = :taxType " +
           "AND te.conditionKey = :conditionKey AND te.validFrom <= :date " +
           "AND (te.validTo IS NULL OR te.validTo >= :date)")
    boolean isExempt(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("conditionKey") String conditionKey,
            @Param("date") LocalDate date);
}
