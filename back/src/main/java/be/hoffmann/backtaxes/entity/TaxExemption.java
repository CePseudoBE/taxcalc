package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;

/**
 * Conditions d'exemption totale de taxe.
 *
 * Certains vehicules sont completement exoneres de taxes:
 *   - Vehicules electriques (dans toutes les regions)
 *   - Vehicules hydrogene (dans toutes les regions)
 *
 * La condition_key indique le critere d'exemption:
 *   - "fuel_electric": exonere si carburant = electric
 *   - "fuel_hydrogen": exonere si carburant = hydrogen
 *
 * Pour verifier si un vehicule est exempte, on cherche une exemption
 * qui correspond a ses caracteristiques.
 */
@Entity
@Table(name = "tax_exemptions")
public class TaxExemption {

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
     * Cle de la condition d'exemption.
     * Format: "critere_valeur", ex: "fuel_electric", "fuel_hydrogen"
     */
    @Column(name = "condition_key", nullable = false, length = 50)
    private String conditionKey;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_to")
    private LocalDate validTo;

    // ==================== CONSTRUCTEURS ====================

    public TaxExemption() {
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

    public String getConditionKey() {
        return conditionKey;
    }

    public void setConditionKey(String conditionKey) {
        this.conditionKey = conditionKey;
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
