package be.hoffmann.backtaxes.dto.response;

import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * Reponse pour un calcul de taxe.
 * Inclut le montant et le detail du calcul.
 */
public class TaxCalculationResponse {

    private Region region;
    private TaxType taxType;
    private BigDecimal amount;
    private Boolean isExempt;
    private String exemptionReason;
    private Map<String, Object> breakdown;

    public TaxCalculationResponse() {
        this.breakdown = new HashMap<>();
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Boolean getIsExempt() {
        return isExempt;
    }

    public void setIsExempt(Boolean isExempt) {
        this.isExempt = isExempt;
    }

    public String getExemptionReason() {
        return exemptionReason;
    }

    public void setExemptionReason(String exemptionReason) {
        this.exemptionReason = exemptionReason;
    }

    public Map<String, Object> getBreakdown() {
        return breakdown;
    }

    public void setBreakdown(Map<String, Object> breakdown) {
        this.breakdown = breakdown;
    }

    public void addBreakdownItem(String key, Object value) {
        this.breakdown.put(key, value);
    }
}
