package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.TaxParameter;
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
public interface TaxParameterRepository extends JpaRepository<TaxParameter, Long> {

    List<TaxParameter> findByRegionAndTaxType(Region region, TaxType taxType);

    @Query("SELECT tp FROM TaxParameter tp WHERE tp.region = :region AND tp.taxType = :taxType " +
           "AND tp.paramKey = :paramKey AND tp.validFrom <= :date " +
           "AND (tp.validTo IS NULL OR tp.validTo >= :date)")
    Optional<TaxParameter> findValidParameter(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("paramKey") String paramKey,
            @Param("date") LocalDate date);

    @Query("SELECT tp FROM TaxParameter tp WHERE tp.region = :region AND tp.taxType = :taxType " +
           "AND tp.validFrom <= :date AND (tp.validTo IS NULL OR tp.validTo >= :date)")
    List<TaxParameter> findAllValidParameters(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("date") LocalDate date);

    /**
     * Trouve les parametres valides a une date donnee avec filtres optionnels.
     */
    @Query("SELECT tp FROM TaxParameter tp WHERE tp.validFrom <= :date " +
           "AND (tp.validTo IS NULL OR tp.validTo >= :date) " +
           "AND (:region IS NULL OR tp.region = :region) " +
           "AND (:taxType IS NULL OR tp.taxType = :taxType)")
    List<TaxParameter> findValidParametersWithFilters(
            @Param("date") LocalDate date,
            @Param("region") Region region,
            @Param("taxType") TaxType taxType);
}
