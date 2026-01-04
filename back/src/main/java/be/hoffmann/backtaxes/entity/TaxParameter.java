package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Parametres de configuration des taxes.
 *
 * Contrairement aux TaxBracket (tranches), ces parametres sont des
 * valeurs uniques utilisees dans les formules de calcul.
 *
 * Exemples de parametres:
 *   - co2_reference_wltp: 136 g/km (seuil de reference CO2)
 *   - mma_reference: 1838 kg (poids de reference)
 *   - min_amount: 50 EUR (montant minimum de la taxe)
 *   - max_amount: 9000 EUR (montant maximum)
 *   - energy_thermal: 1.0 (coefficient pour vehicules thermiques)
 *   - energy_hybrid: 0.8 (coefficient pour hybrides)
 *   - lpg_reduction: 298 EUR (reduction pour GPL a Bruxelles)
 */
@Entity
@Table(
    name = "tax_parameters",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"region", "tax_type", "param_key", "valid_from"}
    )
)
public class TaxParameter {

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

    /** Cle identifiant le parametre */
    @Column(name = "param_key", nullable = false, length = 50)
    private String paramKey;

    /**
     * Valeur du parametre.
     * precision = 12, scale = 4 -> XXXX XXXX.XXXX
     * Permet de stocker des montants (50.00) ou des coefficients (0.0030)
     */
    @Column(name = "param_value", nullable = false, precision = 12, scale = 4)
    private BigDecimal paramValue;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    // ==================== CONSTRUCTEURS ====================

    public TaxParameter() {
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

    public String getParamKey() {
        return paramKey;
    }

    public void setParamKey(String paramKey) {
        this.paramKey = paramKey;
    }

    public BigDecimal getParamValue() {
        return paramValue;
    }

    public void setParamValue(BigDecimal paramValue) {
        this.paramValue = paramValue;
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
