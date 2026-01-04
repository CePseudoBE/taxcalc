package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.PopularVehicle;
import be.hoffmann.backtaxes.entity.enums.RankingType;
import be.hoffmann.backtaxes.entity.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PopularVehicleRepository extends JpaRepository<PopularVehicle, Long> {

    List<PopularVehicle> findByWeekStart(LocalDate weekStart);

    List<PopularVehicle> findByWeekStartAndRegion(LocalDate weekStart, Region region);

    List<PopularVehicle> findByWeekStartAndRankingType(LocalDate weekStart, RankingType rankingType);

    List<PopularVehicle> findByWeekStartAndRegionAndRankingType(LocalDate weekStart, Region region, RankingType rankingType);

    List<PopularVehicle> findByWeekStartAndRegionAndRankingTypeOrderByRankAsc(LocalDate weekStart, Region region, RankingType rankingType);

    List<PopularVehicle> findByVariantId(Long variantId);

    List<PopularVehicle> findByBrandId(Long brandId);

    @Query("SELECT pv FROM PopularVehicle pv WHERE pv.weekStart = :weekStart AND pv.region = :region AND pv.rankingType = :rankingType AND pv.rank <= :topN ORDER BY pv.rank ASC")
    List<PopularVehicle> findTopN(@Param("weekStart") LocalDate weekStart, @Param("region") Region region, @Param("rankingType") RankingType rankingType, @Param("topN") int topN);

    @Query("SELECT DISTINCT pv.weekStart FROM PopularVehicle pv ORDER BY pv.weekStart DESC")
    List<LocalDate> findDistinctWeekStarts();

    @Query("SELECT pv.weekStart, pv.region, pv.rankingType, pv.variant.id, pv.rank FROM PopularVehicle pv WHERE pv.variant.id = :variantId ORDER BY pv.weekStart DESC")
    List<Object[]> findRankingHistoryByVariant(@Param("variantId") Long variantId);

    boolean existsByWeekStart(LocalDate weekStart);

    void deleteByWeekStart(LocalDate weekStart);
}
