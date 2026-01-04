package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.RankingType;
import be.hoffmann.backtaxes.entity.enums.Region;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Snapshot hebdomadaire des vehicules populaires.
 *
 * Cette table stocke le classement des vehicules selon differentes
 * metriques, genere chaque semaine par un job batch:
 *   - most_searched: Top 100 vehicules les plus recherches
 *   - highest_tax: Top 100 vehicules avec la taxe la plus elevee
 *   - lowest_tax: Top 100 vehicules avec la taxe la plus basse
 *
 * Un classement est genere par region et par type, permettant
 * des analyses regionales detaillees.
 *
 * Contrainte unique: (week_start, region, ranking_type, rank)
 * garantit un seul vehicule par position dans chaque classement.
 */
@Entity
@Table(name = "popular_vehicles")
public class PopularVehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Debut de la semaine (lundi) */
    @Column(name = "week_start", nullable = false)
    private LocalDate weekStart;

    /** Region du classement */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "region")
    private Region region;

    /** Type de classement */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "ranking_type", nullable = false, columnDefinition = "ranking_type")
    private RankingType rankingType;

    /** Position dans le classement (1-100) */
    @Column(nullable = false)
    private Integer rank;

    /** Variante classee */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id", nullable = false)
    private Variant variant;

    /** Marque (denormalise pour requetes rapides) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    /** Modele (denormalise pour requetes rapides) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    /** Nombre de recherches sur la semaine */
    @Column(name = "search_count")
    private Integer searchCount = 0;

    /** TMC moyenne calculee */
    @Column(name = "avg_tax_tmc", precision = 12, scale = 2)
    private BigDecimal avgTaxTmc;

    /** Taxe annuelle moyenne calculee */
    @Column(name = "avg_tax_annual", precision = 12, scale = 2)
    private BigDecimal avgTaxAnnual;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // ==================== CONSTRUCTEURS ====================

    public PopularVehicle() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getWeekStart() {
        return weekStart;
    }

    public void setWeekStart(LocalDate weekStart) {
        this.weekStart = weekStart;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public RankingType getRankingType() {
        return rankingType;
    }

    public void setRankingType(RankingType rankingType) {
        this.rankingType = rankingType;
    }

    public Integer getRank() {
        return rank;
    }

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Integer getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }

    public BigDecimal getAvgTaxTmc() {
        return avgTaxTmc;
    }

    public void setAvgTaxTmc(BigDecimal avgTaxTmc) {
        this.avgTaxTmc = avgTaxTmc;
    }

    public BigDecimal getAvgTaxAnnual() {
        return avgTaxAnnual;
    }

    public void setAvgTaxAnnual(BigDecimal avgTaxAnnual) {
        this.avgTaxAnnual = avgTaxAnnual;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
