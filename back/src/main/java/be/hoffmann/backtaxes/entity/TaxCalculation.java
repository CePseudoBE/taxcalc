package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * Resultat d'un calcul de taxe effectue.
 *
 * Enregistre les details du calcul pour permettre:
 *   - Analyse des montants de taxe par vehicule/region
 *   - Identification des vehicules les plus/moins taxes
 *   - Statistiques sur les caracteristiques techniques (CO2, puissance)
 *
 * Lie a un SearchEvent pour tracer le parcours utilisateur.
 */
@Entity
@Table(name = "tax_calculations")
public class TaxCalculation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Evenement de recherche ayant declenche ce calcul */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "search_event_id", nullable = false)
    private SearchEvent searchEvent;

    /** Variante du catalogue officiel (optionnel) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private Variant variant;

    /** Soumission utilisateur (optionnel, si vehicule non catalogue) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private VehicleSubmission submission;

    /** Region pour laquelle le calcul a ete fait */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "region")
    private Region region;

    /** Type de taxe calculee (TMC ou annuelle) */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tax_type", nullable = false, columnDefinition = "tax_type")
    private TaxType taxType;

    /** Montant de taxe calcule */
    @Column(name = "calculated_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal calculatedAmount;

    /** Puissance en kW utilisee pour le calcul */
    @Column(name = "power_kw")
    private Integer powerKw;

    /** Chevaux fiscaux utilises */
    @Column(name = "cv_fiscal")
    private Integer cvFiscal;

    /** Emissions CO2 en g/km */
    @Column(name = "co2_gkm")
    private Integer co2Gkm;

    /** Age du vehicule en mois au moment du calcul */
    @Column(name = "vehicle_age_months")
    private Integer vehicleAgeMonths;

    /** Vehicule exonere (electrique, hydrogene) */
    @Column(name = "is_exempt")
    private Boolean isExempt = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // ==================== CONSTRUCTEURS ====================

    public TaxCalculation() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SearchEvent getSearchEvent() {
        return searchEvent;
    }

    public void setSearchEvent(SearchEvent searchEvent) {
        this.searchEvent = searchEvent;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public VehicleSubmission getSubmission() {
        return submission;
    }

    public void setSubmission(VehicleSubmission submission) {
        this.submission = submission;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public TaxType getTaxType() {
        return taxType;
    }

    public void setTaxType(TaxType taxType) {
        this.taxType = taxType;
    }

    public BigDecimal getCalculatedAmount() {
        return calculatedAmount;
    }

    public void setCalculatedAmount(BigDecimal calculatedAmount) {
        this.calculatedAmount = calculatedAmount;
    }

    public Integer getPowerKw() {
        return powerKw;
    }

    public void setPowerKw(Integer powerKw) {
        this.powerKw = powerKw;
    }

    public Integer getCvFiscal() {
        return cvFiscal;
    }

    public void setCvFiscal(Integer cvFiscal) {
        this.cvFiscal = cvFiscal;
    }

    public Integer getCo2Gkm() {
        return co2Gkm;
    }

    public void setCo2Gkm(Integer co2Gkm) {
        this.co2Gkm = co2Gkm;
    }

    public Integer getVehicleAgeMonths() {
        return vehicleAgeMonths;
    }

    public void setVehicleAgeMonths(Integer vehicleAgeMonths) {
        this.vehicleAgeMonths = vehicleAgeMonths;
    }

    public Boolean getIsExempt() {
        return isExempt;
    }

    public void setIsExempt(Boolean isExempt) {
        this.isExempt = isExempt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
