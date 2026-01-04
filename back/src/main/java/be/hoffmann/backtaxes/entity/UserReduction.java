package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Reduction concrete applicable pour un type donne, par region.
 *
 * Exemple: En Wallonie, les familles nombreuses ont -250 EUR sur la TMC.
 *
 * La reduction peut etre:
 *   - Un montant fixe (is_percentage = false): -250 EUR
 *   - Un pourcentage (is_percentage = true): -10%
 *
 * Certaines reductions ont des conditions supplementaires:
 *   - max_co2: Le vehicule ne doit pas depasser X g/km de CO2
 */
@Entity
@Table(name = "user_reductions")
public class UserReduction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "region")
    private Region region;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "tax_type", nullable = false, columnDefinition = "tax_type")
    private TaxType taxType;

    /** Le type de reduction (famille nombreuse, etc.) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reduction_type_id", nullable = false)
    private UserReductionType reductionType;

    /**
     * true = la valeur est un pourcentage (ex: 10 = 10%)
     * false = la valeur est un montant fixe (ex: 250 = 250 EUR)
     */
    @Column(name = "is_percentage", nullable = false)
    private Boolean isPercentage;

    /** Valeur de la reduction (montant ou pourcentage selon is_percentage) */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    /**
     * Condition optionnelle: CO2 maximum pour eligibilite.
     * Si le vehicule depasse ce seuil, la reduction ne s'applique pas.
     */
    @Column(name = "max_co2")
    private Integer maxCo2;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    // ==================== CONSTRUCTEURS ====================

    public UserReduction() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public UserReductionType getReductionType() {
        return reductionType;
    }

    public void setReductionType(UserReductionType reductionType) {
        this.reductionType = reductionType;
    }

    public Boolean getIsPercentage() {
        return isPercentage;
    }

    public void setIsPercentage(Boolean percentage) {
        isPercentage = percentage;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public Integer getMaxCo2() {
        return maxCo2;
    }

    public void setMaxCo2(Integer maxCo2) {
        this.maxCo2 = maxCo2;
    }

    public LocalDate getValidFrom() {
        return validFrom;
    }

    public void setValidFrom(LocalDate validFrom) {
        this.validFrom = validFrom;
    }

    public LocalDate getValidTo() {
        return validTo;
    }

    public void setValidTo(LocalDate validTo) {
        this.validTo = validTo;
    }

    // ==================== METHODES UTILITAIRES ====================

    public boolean isValidAt(LocalDate date) {
        return !date.isBefore(validFrom) && (validTo == null || !date.isAfter(validTo));
    }

    /**
     * Verifie si un vehicule avec ce CO2 est eligible a cette reduction.
     */
    public boolean isEligibleForCo2(Integer vehicleCo2) {
        if (maxCo2 == null) {
            return true; // Pas de limite CO2
        }
        return vehicleCo2 != null && vehicleCo2 <= maxCo2;
    }
}
