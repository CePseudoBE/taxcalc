package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.AgeCoefficient;
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
public interface AgeCoefficientRepository extends JpaRepository<AgeCoefficient, Long> {

    List<AgeCoefficient> findByRegionAndTaxType(Region region, TaxType taxType);

    @Query("SELECT ac FROM AgeCoefficient ac WHERE ac.region = :region AND ac.taxType = :taxType " +
           "AND ac.vehicleAgeYears = :age AND ac.validFrom <= :date " +
           "AND (ac.validTo IS NULL OR ac.validTo >= :date)")
    Optional<AgeCoefficient> findValidCoefficient(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("age") int age,
            @Param("date") LocalDate date);

    @Query("SELECT ac FROM AgeCoefficient ac WHERE ac.region = :region AND ac.taxType = :taxType " +
           "AND ac.validFrom <= :date AND (ac.validTo IS NULL OR ac.validTo >= :date) " +
           "ORDER BY ac.vehicleAgeYears")
    List<AgeCoefficient> findAllValidCoefficients(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("date") LocalDate date);
}
