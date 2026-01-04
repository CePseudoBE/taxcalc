package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.DailyAggregate;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface DailyAggregateRepository extends JpaRepository<DailyAggregate, Long> {

    List<DailyAggregate> findByDate(LocalDate date);

    List<DailyAggregate> findByDateBetween(LocalDate start, LocalDate end);

    List<DailyAggregate> findByRegion(Region region);

    List<DailyAggregate> findByDateAndRegion(LocalDate date, Region region);

    List<DailyAggregate> findByBrandId(Long brandId);

    List<DailyAggregate> findByDateBetweenAndRegion(LocalDate start, LocalDate end, Region region);

    List<DailyAggregate> findByDateBetweenAndBrandId(LocalDate start, LocalDate end, Long brandId);

    @Query("SELECT da FROM DailyAggregate da WHERE da.date BETWEEN :start AND :end AND da.region IS NULL AND da.brand IS NULL ORDER BY da.date")
    List<DailyAggregate> findGlobalAggregatesBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT SUM(da.searchCount), SUM(da.calculationCount), SUM(da.uniqueSessions) FROM DailyAggregate da WHERE da.date BETWEEN :start AND :end AND da.region IS NULL AND da.brand IS NULL")
    Object[] sumGlobalStatsBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT da.brand.id, SUM(da.searchCount) FROM DailyAggregate da WHERE da.date BETWEEN :start AND :end AND da.brand IS NOT NULL AND da.model IS NULL GROUP BY da.brand.id ORDER BY SUM(da.searchCount) DESC")
    List<Object[]> findTopBrandsBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    @Query("SELECT da.fuelType, SUM(da.searchCount) FROM DailyAggregate da WHERE da.date BETWEEN :start AND :end AND da.fuelType IS NOT NULL GROUP BY da.fuelType ORDER BY SUM(da.searchCount) DESC")
    List<Object[]> findSearchCountByFuelTypeBetween(@Param("start") LocalDate start, @Param("end") LocalDate end);

    boolean existsByDate(LocalDate date);
}
