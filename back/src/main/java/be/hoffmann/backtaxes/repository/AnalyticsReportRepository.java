package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.AnalyticsReport;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.ReportType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AnalyticsReportRepository extends JpaRepository<AnalyticsReport, Long> {

    List<AnalyticsReport> findByReportType(ReportType reportType);

    List<AnalyticsReport> findByRegion(Region region);

    List<AnalyticsReport> findByIsPublic(Boolean isPublic);

    List<AnalyticsReport> findByReportTypeAndRegion(ReportType reportType, Region region);

    List<AnalyticsReport> findByPeriodStartBetween(LocalDate start, LocalDate end);

    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.reportType = :reportType ORDER BY ar.periodEnd DESC")
    List<AnalyticsReport> findLatestByType(@Param("reportType") ReportType reportType);

    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.isPublic = true ORDER BY ar.createdAt DESC")
    List<AnalyticsReport> findPublicReports();

    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.reportType = :reportType AND ar.region = :region ORDER BY ar.periodEnd DESC")
    List<AnalyticsReport> findByTypeAndRegionOrderByPeriodDesc(@Param("reportType") ReportType reportType, @Param("region") Region region);

    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.periodStart <= :date AND ar.periodEnd >= :date")
    List<AnalyticsReport> findReportsCoveringDate(@Param("date") LocalDate date);

    @Query("SELECT ar FROM AnalyticsReport ar WHERE ar.reportType = 'data_retention' ORDER BY ar.periodEnd DESC")
    List<AnalyticsReport> findRetentionReports();

    boolean existsByReportTypeAndPeriodStartAndPeriodEnd(ReportType reportType, LocalDate periodStart, LocalDate periodEnd);
}
