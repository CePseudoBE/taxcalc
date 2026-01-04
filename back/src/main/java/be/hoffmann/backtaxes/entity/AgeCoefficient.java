package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Coefficient degressif selon l'age du vehicule.
 *
 * La TMC diminue avec l'age du vehicule:
 *   - Vehicule neuf (0 an): coefficient 1.00 (100%)
 *   - 1 an: coefficient 0.90 (90%)
 *   - 2 ans: coefficient 0.80 (80%)
 *   - ...
 *   - 14 ans: coefficient 0.10 (10%)
 *   - 15+ ans: coefficient 0.00 (exonere!)
 *
 * Cela encourage l'achat de vehicules d'occasion.
 */
@Entity
@Table(
    name = "age_coefficients",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"region", "tax_type", "vehicle_age_years", "valid_from"}
    )
)
public class AgeCoefficient {

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

    /** Age du vehicule en annees (0 = neuf) */
    @Column(name = "vehicle_age_years", nullable = false)
    private Integer vehicleAgeYears;

    /**
     * Coefficient a appliquer (entre 0.0000 et 1.0000).
     *
     * precision = 5, scale = 4 -> format X.XXXX
     * Exemples: 1.0000, 0.9000, 0.1000, 0.0000
     */
    @Column(nullable = false, precision = 5, scale = 4)
    private BigDecimal coefficient;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    // ==================== CONSTRUCTEURS ====================

    public AgeCoefficient() {
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

    public Integer getVehicleAgeYears() {
        return vehicleAgeYears;
    }

    public void setVehicleAgeYears(Integer vehicleAgeYears) {
        this.vehicleAgeYears = vehicleAgeYears;
    }

    public BigDecimal getCoefficient() {
        return coefficient;
    }

    public void setCoefficient(BigDecimal coefficient) {
        this.coefficient = coefficient;
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
}
