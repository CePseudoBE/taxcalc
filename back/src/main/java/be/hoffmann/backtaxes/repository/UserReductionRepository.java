package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.UserReduction;
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
public interface UserReductionRepository extends JpaRepository<UserReduction, Long> {

    List<UserReduction> findByRegionAndTaxType(Region region, TaxType taxType);

    @Query("SELECT ur FROM UserReduction ur JOIN ur.reductionType rt " +
           "WHERE ur.region = :region AND ur.taxType = :taxType AND rt.code = :typeCode " +
           "AND ur.validFrom <= :date AND (ur.validTo IS NULL OR ur.validTo >= :date)")
    Optional<UserReduction> findValidReduction(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("typeCode") String typeCode,
            @Param("date") LocalDate date);

    @Query("SELECT ur FROM UserReduction ur WHERE ur.region = :region AND ur.taxType = :taxType " +
           "AND ur.validFrom <= :date AND (ur.validTo IS NULL OR ur.validTo >= :date)")
    List<UserReduction> findAllValidReductions(
            @Param("region") Region region,
            @Param("taxType") TaxType taxType,
            @Param("date") LocalDate date);
}
