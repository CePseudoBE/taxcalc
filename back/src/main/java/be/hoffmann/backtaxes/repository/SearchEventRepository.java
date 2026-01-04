package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.SearchEvent;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.SearchType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SearchEventRepository extends JpaRepository<SearchEvent, Long> {

    List<SearchEvent> findBySessionId(UUID sessionId);

    List<SearchEvent> findByUserId(Long userId);

    List<SearchEvent> findByBrandId(Long brandId);

    List<SearchEvent> findByVariantId(Long variantId);

    List<SearchEvent> findByCreatedAtBetween(Instant start, Instant end);

    List<SearchEvent> findByRegionAndCreatedAtBetween(Region region, Instant start, Instant end);

    @Query("SELECT se FROM SearchEvent se WHERE se.createdAt >= :since ORDER BY se.createdAt DESC")
    List<SearchEvent> findRecentEvents(@Param("since") Instant since);

    @Query("SELECT COUNT(DISTINCT se.sessionId) FROM SearchEvent se WHERE se.createdAt BETWEEN :start AND :end")
    Long countUniqueSessionsBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT se.brand.id, COUNT(se) FROM SearchEvent se WHERE se.createdAt BETWEEN :start AND :end AND se.brand IS NOT NULL GROUP BY se.brand.id ORDER BY COUNT(se) DESC")
    List<Object[]> countByBrandBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT se.region, COUNT(se) FROM SearchEvent se WHERE se.createdAt BETWEEN :start AND :end AND se.region IS NOT NULL GROUP BY se.region")
    List<Object[]> countByRegionBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT se.fuelType, COUNT(se) FROM SearchEvent se WHERE se.createdAt BETWEEN :start AND :end AND se.fuelType IS NOT NULL GROUP BY se.fuelType")
    List<Object[]> countByFuelTypeBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT se.searchType, COUNT(se) FROM SearchEvent se WHERE se.createdAt BETWEEN :start AND :end GROUP BY se.searchType")
    List<Object[]> countBySearchTypeBetween(@Param("start") Instant start, @Param("end") Instant end);

    @Query("SELECT se.deviceType, COUNT(se) FROM SearchEvent se WHERE se.createdAt BETWEEN :start AND :end AND se.deviceType IS NOT NULL GROUP BY se.deviceType")
    List<Object[]> countByDeviceTypeBetween(@Param("start") Instant start, @Param("end") Instant end);

    Long countByIsNewVehicleAndCreatedAtBetween(Boolean isNewVehicle, Instant start, Instant end);

    void deleteByCreatedAtBefore(Instant before);
}
