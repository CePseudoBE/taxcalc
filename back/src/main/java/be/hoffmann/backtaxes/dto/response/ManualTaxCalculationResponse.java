package be.hoffmann.backtaxes.dto.response;

import be.hoffmann.backtaxes.entity.enums.Region;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Reponse structuree pour un calcul manuel de taxes.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ManualTaxCalculationResponse {

    /**
     * Statut global du calcul.
     */
    public enum Status {
        complete,       // Toutes les taxes calculees
        partial,        // Certaines taxes calculees
        insufficient    // Donnees insuffisantes pour tout calcul
    }

    private Status status;
    private Region region;
    private TaxResults taxes;
    private RegionRequirements requirements;
    private PendingInfo pendingSubmission;
    private String message;

    public ManualTaxCalculationResponse() {
        this.taxes = new TaxResults();
    }

    // ==================== NESTED CLASSES ====================

    /**
     * Resultats des calculs de taxes.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TaxResults {
        private TaxResult tmc;
        private TaxResult annual;

        public TaxResults() {
            this.tmc = new TaxResult();
            this.annual = new TaxResult();
        }

        public TaxResult getTmc() { return tmc; }
        public void setTmc(TaxResult tmc) { this.tmc = tmc; }
        public TaxResult getAnnual() { return annual; }
        public void setAnnual(TaxResult annual) { this.annual = annual; }
    }

    /**
     * Resultat d'un calcul de taxe individuel.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class TaxResult {
        private boolean calculable;
        private BigDecimal amount;
        private boolean exempt;
        private String exemptionReason;
        private Map<String, Object> breakdown;
        private List<String> missingFields;

        public boolean isCalculable() { return calculable; }
        public void setCalculable(boolean calculable) { this.calculable = calculable; }
        public BigDecimal getAmount() { return amount; }
        public void setAmount(BigDecimal amount) { this.amount = amount; }
        public boolean isExempt() { return exempt; }
        public void setExempt(boolean exempt) { this.exempt = exempt; }
        public String getExemptionReason() { return exemptionReason; }
        public void setExemptionReason(String exemptionReason) { this.exemptionReason = exemptionReason; }
        public Map<String, Object> getBreakdown() { return breakdown; }
        public void setBreakdown(Map<String, Object> breakdown) { this.breakdown = breakdown; }
        public List<String> getMissingFields() { return missingFields; }
        public void setMissingFields(List<String> missingFields) { this.missingFields = missingFields; }

        /**
         * Remplit ce resultat depuis un TaxCalculationResponse.
         */
        public void fromTaxCalculationResponse(TaxCalculationResponse response) {
            this.calculable = true;
            this.amount = response.getAmount();
            this.exempt = Boolean.TRUE.equals(response.getIsExempt());
            this.exemptionReason = response.getExemptionReason();
            this.breakdown = response.getBreakdown();
        }
    }

    /**
     * Champs requis pour cette region.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RegionRequirements {
        private List<FieldRequirement> tmc;
        private List<FieldRequirement> annual;

        public List<FieldRequirement> getTmc() { return tmc; }
        public void setTmc(List<FieldRequirement> tmc) { this.tmc = tmc; }
        public List<FieldRequirement> getAnnual() { return annual; }
        public void setAnnual(List<FieldRequirement> annual) { this.annual = annual; }
    }

    /**
     * Description d'un champ requis.
     */
    public static class FieldRequirement {
        private String field;
        private String description;
        private boolean provided;

        public FieldRequirement() {}

        public FieldRequirement(String field, String description, boolean provided) {
            this.field = field;
            this.description = description;
            this.provided = provided;
        }

        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public boolean isProvided() { return provided; }
        public void setProvided(boolean provided) { this.provided = provided; }
    }

    /**
     * Info sur la soumission en attente.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PendingInfo {
        private Long id;
        private String status;
        private String message;

        public PendingInfo() {}

        public PendingInfo(Long id, String status, String message) {
            this.id = id;
            this.status = status;
            this.message = message;
        }

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    // ==================== GETTERS & SETTERS ====================

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Region getRegion() { return region; }
    public void setRegion(Region region) { this.region = region; }

    public TaxResults getTaxes() { return taxes; }
    public void setTaxes(TaxResults taxes) { this.taxes = taxes; }

    public RegionRequirements getRequirements() { return requirements; }
    public void setRequirements(RegionRequirements requirements) { this.requirements = requirements; }

    public PendingInfo getPendingSubmission() { return pendingSubmission; }
    public void setPendingSubmission(PendingInfo pendingSubmission) { this.pendingSubmission = pendingSubmission; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    // ==================== HELPER METHODS ====================

    /**
     * Determine le statut global en fonction des resultats.
     */
    public void computeStatus() {
        boolean tmcOk = taxes.getTmc() != null && taxes.getTmc().isCalculable();
        boolean annualOk = taxes.getAnnual() != null && taxes.getAnnual().isCalculable();

        if (tmcOk && annualOk) {
            this.status = Status.complete;
        } else if (tmcOk || annualOk) {
            this.status = Status.partial;
        } else {
            this.status = Status.insufficient;
        }
    }
}
