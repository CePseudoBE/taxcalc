package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Agregats quotidiens pre-calcules pour les requetes rapides.
 *
 * Cette table stocke des statistiques agregees par jour, permettant
 * des requetes performantes sur les tendances sans scanner les
 * millions de lignes de search_events.
 *
 * Les agregats sont calcules par un job batch quotidien et conserves
 * indefiniment (contrairement aux donnees brutes purgees apres 90j).
 *
 * Les colonnes nullables (region, brand_id, etc.) permettent differents
 * niveaux d'agregation:
 *   - Tout null = agregat global du jour
 *   - region seul = agregat par region
 *   - region + brand_id = agregat par marque dans une region
 *   - etc.
 */
@Entity
@Table(name = "daily_aggregates")
public class DailyAggregate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Jour de l'agregat */
    @Column(nullable = false)
    private LocalDate date;

    /** Region (null = toutes regions) */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "region")
    private Region region;

    /** Marque (null = toutes marques) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /** Modele (null = tous modeles) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;

    /** Type de carburant (null = tous) */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "fuel_type", columnDefinition = "fuel_type")
    private FuelType fuelType;

    /** Neuf/occasion (null = tous) */
    @Column(name = "is_new_vehicle")
    private Boolean isNewVehicle;

    /** Nombre de recherches */
    @Column(name = "search_count")
    private Integer searchCount = 0;

    /** Nombre de calculs de taxe effectues */
    @Column(name = "calculation_count")
    private Integer calculationCount = 0;

    /** Nombre de sessions uniques */
    @Column(name = "unique_sessions")
    private Integer uniqueSessions = 0;

    /** Montant de taxe moyen */
    @Column(name = "avg_tax_amount", precision = 12, scale = 2)
    private BigDecimal avgTaxAmount;

    /** Montant de taxe maximum */
    @Column(name = "max_tax_amount", precision = 12, scale = 2)
    private BigDecimal maxTaxAmount;

    /** Montant de taxe minimum */
    @Column(name = "min_tax_amount", precision = 12, scale = 2)
    private BigDecimal minTaxAmount;

    // ==================== CONSTRUCTEURS ====================

    public DailyAggregate() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
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

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Boolean getIsNewVehicle() {
        return isNewVehicle;
    }

    public void setIsNewVehicle(Boolean isNewVehicle) {
        this.isNewVehicle = isNewVehicle;
    }

    public Integer getSearchCount() {
        return searchCount;
    }

    public void setSearchCount(Integer searchCount) {
        this.searchCount = searchCount;
    }

    public Integer getCalculationCount() {
        return calculationCount;
    }

    public void setCalculationCount(Integer calculationCount) {
        this.calculationCount = calculationCount;
    }

    public Integer getUniqueSessions() {
        return uniqueSessions;
    }

    public void setUniqueSessions(Integer uniqueSessions) {
        this.uniqueSessions = uniqueSessions;
    }

    public BigDecimal getAvgTaxAmount() {
        return avgTaxAmount;
    }

    public void setAvgTaxAmount(BigDecimal avgTaxAmount) {
        this.avgTaxAmount = avgTaxAmount;
    }

    public BigDecimal getMaxTaxAmount() {
        return maxTaxAmount;
    }

    public void setMaxTaxAmount(BigDecimal maxTaxAmount) {
        this.maxTaxAmount = maxTaxAmount;
    }

    public BigDecimal getMinTaxAmount() {
        return minTaxAmount;
    }

    public void setMinTaxAmount(BigDecimal minTaxAmount) {
        this.minTaxAmount = minTaxAmount;
    }
}
