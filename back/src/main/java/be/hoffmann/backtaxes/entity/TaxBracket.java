package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Tranche de taxation.
 *
 * Exemple: Pour la TMC en Wallonie, si la puissance est entre 0 et 70 kW,
 * le montant de base est de 61.50 EUR.
 *
 * Les tranches sont definies par:
 *   - region + tax_type: pour quelle taxe/region
 *   - bracket_key: le type de tranche (puissance kW, CV fiscaux, etc.)
 *   - min_value/max_value: la plage de valeurs
 *   - amount: le montant ou coefficient associe
 *   - valid_from/valid_to: periode de validite (les taxes changent dans le temps!)
 */
@Entity
@Table(name = "tax_brackets")
public class TaxBracket {

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

    /**
     * Cle identifiant le type de tranche:
     *   - "power_kw": tranches de puissance en kW
     *   - "fiscal_hp": tranches de CV fiscaux
     *   - "energy_electric_kw": coefficient energie pour electrique
     *   - "euro_norm_factor": facteur selon norme Euro
     */
    @Column(name = "bracket_key", nullable = false, length = 50)
    private String bracketKey;

    /** Valeur minimum de la tranche (incluse) */
    @Column(name = "min_value", nullable = false)
    private Integer minValue;

    /** Valeur maximum de la tranche (incluse). Null = pas de limite */
    @Column(name = "max_value")
    private Integer maxValue;

    /**
     * Montant ou coefficient associe a cette tranche.
     *
     * BigDecimal pour les montants monetaires:
     *   - Evite les erreurs d'arrondi des float/double
     *   - Precision exacte pour les calculs financiers
     *   - (12,2) = 12 chiffres au total, dont 2 decimales
     */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    /** Date de debut de validite de cette tranche */
    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    /** Date de fin de validite. Null = toujours valide */
    @Column(name = "valid_to")
    private LocalDate validTo;

    // ==================== CONSTRUCTEURS ====================

    public TaxBracket() {
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

    public String getBracketKey() {
        return bracketKey;
    }

    public void setBracketKey(String bracketKey) {
        this.bracketKey = bracketKey;
    }

    public Integer getMinValue() {
        return minValue;
    }

    public void setMinValue(Integer minValue) {
        this.minValue = minValue;
    }

    public Integer getMaxValue() {
        return maxValue;
    }

    public void setMaxValue(Integer maxValue) {
        this.maxValue = maxValue;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
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

    /**
     * Verifie si une valeur tombe dans cette tranche.
     */
    public boolean contains(int value) {
        return value >= minValue && (maxValue == null || value <= maxValue);
    }

    /**
     * Verifie si cette tranche est valide a une date donnee.
     */
    public boolean isValidAt(LocalDate date) {
        return !date.isBefore(validFrom) && (validTo == null || !date.isAfter(validTo));
    }
}
