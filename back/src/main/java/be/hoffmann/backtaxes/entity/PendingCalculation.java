package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Calcul en attente de completion par les moderateurs.
 *
 * Workflow:
 * 1. Un utilisateur soumet des donnees partielles pour calculer ses taxes
 * 2. Si les donnees sont insuffisantes pour un calcul complet, il peut demander
 *    aux moderateurs de completer les informations manquantes
 * 3. Un moderateur complete les donnees et cree une VehicleSubmission complete
 */
@Entity
@Table(name = "pending_calculations")
public class PendingCalculation {

    public enum Status {
        pending,    // En attente de moderation
        completed,  // Complete par un moderateur
        rejected    // Rejete (donnees insuffisantes ou invalides)
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id")
    private UUID sessionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "region")
    private Region region;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.pending;

    // ==================== DONNEES VEHICULE (optionnelles) ====================

    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "model_name", length = 100)
    private String modelName;

    @Column(name = "variant_name", length = 150)
    private String variantName;

    @Column(name = "year_start")
    private Integer yearStart;

    @Column(name = "power_kw")
    private Integer powerKw;

    @Column(name = "fiscal_hp")
    private Integer fiscalHp;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "fuel_type")
    private FuelType fuel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "euro_norm", columnDefinition = "euro_norm")
    private EuroNorm euroNorm;

    @Column(name = "co2_wltp")
    private Integer co2Wltp;

    @Column(name = "co2_nedc")
    private Integer co2Nedc;

    @Column(name = "mma_kg")
    private Integer mmaKg;

    // ==================== RESULTATS CALCUL ====================

    @Column(name = "calculated_tmc", precision = 12, scale = 2)
    private BigDecimal calculatedTmc;

    @Column(name = "calculated_annual", precision = 12, scale = 2)
    private BigDecimal calculatedAnnual;

    @Column(name = "tmc_calculable")
    private Boolean tmcCalculable = false;

    @Column(name = "annual_calculable")
    private Boolean annualCalculable = false;

    // ==================== MODERATION ====================

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewer;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_submission_id")
    private VehicleSubmission createdSubmission;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private Instant updatedAt;

    // ==================== CONSTRUCTEURS ====================

    public PendingCalculation() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getVariantName() {
        return variantName;
    }

    public void setVariantName(String variantName) {
        this.variantName = variantName;
    }

    public Integer getYearStart() {
        return yearStart;
    }

    public void setYearStart(Integer yearStart) {
        this.yearStart = yearStart;
    }

    public Integer getPowerKw() {
        return powerKw;
    }

    public void setPowerKw(Integer powerKw) {
        this.powerKw = powerKw;
    }

    public Integer getFiscalHp() {
        return fiscalHp;
    }

    public void setFiscalHp(Integer fiscalHp) {
        this.fiscalHp = fiscalHp;
    }

    public FuelType getFuel() {
        return fuel;
    }

    public void setFuel(FuelType fuel) {
        this.fuel = fuel;
    }

    public EuroNorm getEuroNorm() {
        return euroNorm;
    }

    public void setEuroNorm(EuroNorm euroNorm) {
        this.euroNorm = euroNorm;
    }

    public Integer getCo2Wltp() {
        return co2Wltp;
    }

    public void setCo2Wltp(Integer co2Wltp) {
        this.co2Wltp = co2Wltp;
    }

    public Integer getCo2Nedc() {
        return co2Nedc;
    }

    public void setCo2Nedc(Integer co2Nedc) {
        this.co2Nedc = co2Nedc;
    }

    public Integer getMmaKg() {
        return mmaKg;
    }

    public void setMmaKg(Integer mmaKg) {
        this.mmaKg = mmaKg;
    }

    public BigDecimal getCalculatedTmc() {
        return calculatedTmc;
    }

    public void setCalculatedTmc(BigDecimal calculatedTmc) {
        this.calculatedTmc = calculatedTmc;
    }

    public BigDecimal getCalculatedAnnual() {
        return calculatedAnnual;
    }

    public void setCalculatedAnnual(BigDecimal calculatedAnnual) {
        this.calculatedAnnual = calculatedAnnual;
    }

    public Boolean getTmcCalculable() {
        return tmcCalculable;
    }

    public void setTmcCalculable(Boolean tmcCalculable) {
        this.tmcCalculable = tmcCalculable;
    }

    public Boolean getAnnualCalculable() {
        return annualCalculable;
    }

    public void setAnnualCalculable(Boolean annualCalculable) {
        this.annualCalculable = annualCalculable;
    }

    public User getReviewer() {
        return reviewer;
    }

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public Instant getReviewedAt() {
        return reviewedAt;
    }

    public void setReviewedAt(Instant reviewedAt) {
        this.reviewedAt = reviewedAt;
    }

    public VehicleSubmission getCreatedSubmission() {
        return createdSubmission;
    }

    public void setCreatedSubmission(VehicleSubmission createdSubmission) {
        this.createdSubmission = createdSubmission;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
