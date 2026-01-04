package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.ReportType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Rapport analytics genere et archive.
 *
 * Cette table stocke les rapports generes a partir des donnees analytics,
 * notamment les rapports de retention generes avant la purge des donnees
 * brutes (apres 90 jours).
 *
 * Types de rapports:
 *   - market_trends: Tendances du marche (top marques, modeles)
 *   - tax_analysis: Analyse fiscale (repartition taxes, comparaisons)
 *   - user_behavior: Comportement utilisateur (devices, sources, conversions)
 *   - regional_comparison: Comparaison entre regions
 *   - monthly_summary: Resume mensuel global
 *   - data_retention: Rapport pre-purge des donnees brutes
 *
 * Les donnees du rapport sont stockees en JSONB pour flexibilite,
 * permettant differentes structures selon le type de rapport.
 */
@Entity
@Table(name = "analytics_reports")
public class AnalyticsReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Type de rapport */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "report_type", nullable = false, columnDefinition = "report_type")
    private ReportType reportType;

    /** Titre du rapport */
    @Column(nullable = false, length = 200)
    private String title;

    /** Description du contenu */
    @Column(columnDefinition = "text")
    private String description;

    /** Debut de la periode couverte */
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;

    /** Fin de la periode couverte */
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;

    /** Region concernee (null = toutes regions) */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "region")
    private Region region;

    /**
     * Donnees du rapport en JSON.
     *
     * Structure flexible selon le type de rapport. Exemples:
     *
     * market_trends:
     * {
     *   "top_brands": [{"brand": "BMW", "searches": 1500}, ...],
     *   "top_models": [...],
     *   "fuel_distribution": {...}
     * }
     *
     * tax_analysis:
     * {
     *   "avg_tmc": 2500.00,
     *   "highest_taxed": [...],
     *   "by_fuel_type": {...}
     * }
     *
     * data_retention:
     * {
     *   "total_events_archived": 150000,
     *   "date_range": "2024-01-01 to 2024-03-31",
     *   "summary_stats": {...}
     * }
     */
    @Column(name = "report_data", nullable = false, columnDefinition = "jsonb")
    private String reportData;

    /**
     * Metadonnees du rapport en JSON.
     * Informations techniques: version, parametres de generation, etc.
     */
    @Column(columnDefinition = "jsonb")
    private String metadata;

    /** Rapport accessible publiquement (pour vente) */
    @Column(name = "is_public")
    private Boolean isPublic = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // ==================== CONSTRUCTEURS ====================

    public AnalyticsReport() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public ReportType getReportType() {
        return reportType;
    }

    public void setReportType(ReportType reportType) {
        this.reportType = reportType;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getPeriodStart() {
        return periodStart;
    }

    public void setPeriodStart(LocalDate periodStart) {
        this.periodStart = periodStart;
    }

    public LocalDate getPeriodEnd() {
        return periodEnd;
    }

    public void setPeriodEnd(LocalDate periodEnd) {
        this.periodEnd = periodEnd;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public String getReportData() {
        return reportData;
    }

    public void setReportData(String reportData) {
        this.reportData = reportData;
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
