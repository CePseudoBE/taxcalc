package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.SubmissionStatus;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;

/**
 * Soumission d'un vehicule par un utilisateur.
 *
 * Workflow:
 * 1. Un utilisateur soumet un vehicule qui n'est pas dans le catalogue
 * 2. Un moderateur examine la soumission
 * 3. Si approuvee: un Variant est cree automatiquement
 * 4. Si rejetee: feedback expliquant pourquoi
 *
 * Cette entite a une particularite: DEUX relations vers User!
 *   - submitter: qui a soumis
 *   - reviewer: qui a modere (peut etre null si pas encore modere)
 */
@Entity
@Table(name = "vehicle_submissions")
public class VehicleSubmission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * L'utilisateur qui a soumis ce vehicule.
     *
     * IMPORTANT: Quand tu as plusieurs @ManyToOne vers la meme entite,
     * tu DOIS specifier des noms de colonnes differents!
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User submitter;

    /** Statut de la soumission */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "submission_status")
    private SubmissionStatus status = SubmissionStatus.pending;

    // ==================== DONNEES DU VEHICULE PROPOSE ====================
    // Ces champs sont des copies (pas des FK) car le vehicule n'existe
    // peut-etre pas encore dans le catalogue.

    @Column(name = "brand_name", nullable = false, length = 100)
    private String brandName;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "variant_name", nullable = false, length = 150)
    private String variantName;

    @Column(name = "year_start", nullable = false)
    private Integer yearStart;

    @Column(name = "year_end")
    private Integer yearEnd;

    @Column(name = "displacement_cc")
    private Integer displacementCc;

    @Column(name = "power_kw", nullable = false)
    private Integer powerKw;

    @Column(name = "fiscal_hp", nullable = false)
    private Integer fiscalHp;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "fuel_type")
    private FuelType fuel;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "euro_norm", nullable = false, columnDefinition = "euro_norm")
    private EuroNorm euroNorm;

    @Column(name = "co2_wltp")
    private Integer co2Wltp;

    @Column(name = "co2_nedc")
    private Integer co2Nedc;

    @Column(name = "mma_kg")
    private Integer mmaKg;

    @Column(name = "has_particle_filter")
    private Boolean hasParticleFilter;

    // ==================== MODERATION ====================

    /**
     * Le moderateur qui a traite cette soumission.
     * Null tant que non moderee.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewer;

    /** Date de la moderation */
    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    /** Feedback du moderateur (surtout utile en cas de rejet) */
    @Column(columnDefinition = "text")
    private String feedback;

    /**
     * Si approuvee, reference vers le Variant cree.
     * Permet de lier la soumission au vehicule final dans le catalogue.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_variant_id")
    private Variant createdVariant;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // ==================== CONSTRUCTEURS ====================

    public VehicleSubmission() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getSubmitter() {
        return submitter;
    }

    public void setSubmitter(User submitter) {
        this.submitter = submitter;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
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

    public Integer getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(Integer yearEnd) {
        this.yearEnd = yearEnd;
    }

    public Integer getDisplacementCc() {
        return displacementCc;
    }

    public void setDisplacementCc(Integer displacementCc) {
        this.displacementCc = displacementCc;
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

    public Boolean getHasParticleFilter() {
        return hasParticleFilter;
    }

    public void setHasParticleFilter(Boolean hasParticleFilter) {
        this.hasParticleFilter = hasParticleFilter;
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

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public Variant getCreatedVariant() {
        return createdVariant;
    }

    public void setCreatedVariant(Variant createdVariant) {
        this.createdVariant = createdVariant;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
