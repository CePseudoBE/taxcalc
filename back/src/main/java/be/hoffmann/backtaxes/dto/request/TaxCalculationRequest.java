package be.hoffmann.backtaxes.dto.request;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Requete pour calculer les taxes d'un vehicule.
 * Trois modes possibles (XOR):
 * 1. variantId - vehicule existant dans le catalogue
 * 2. submissionId - soumission en attente de moderation
 * 3. specs manuelles (fiscalHp + fuel) - calcul anonyme sans enregistrement
 */
public class TaxCalculationRequest {

    private Long variantId;

    private Long submissionId;

    // Specs manuelles pour calcul anonyme
    @Min(value = 1, message = "La puissance fiscale doit être au moins 1 CV")
    @Max(value = 100, message = "La puissance fiscale ne peut pas dépasser 100 CV")
    private Integer fiscalHp;

    @Min(value = 1, message = "La puissance doit être au moins 1 kW")
    @Max(value = 1000, message = "La puissance ne peut pas dépasser 1000 kW")
    private Integer powerKw;

    private FuelType fuel;

    private EuroNorm euroNorm;

    @Min(value = 0, message = "Les émissions CO2 ne peuvent pas être négatives")
    @Max(value = 500, message = "Les émissions CO2 ne peuvent pas dépasser 500 g/km")
    private Integer co2Wltp;

    @Min(value = 0, message = "Les émissions CO2 NEDC ne peuvent pas être négatives")
    @Max(value = 400, message = "Les émissions CO2 NEDC ne peuvent pas dépasser 400 g/km")
    private Integer co2Nedc;

    @Min(value = 500, message = "La MMA doit être au moins 500 kg")
    @Max(value = 10000, message = "La MMA ne peut pas dépasser 10000 kg")
    private Integer mmaKg;

    @NotNull(message = "Region is required")
    private Region region;

    @NotNull(message = "La date de première immatriculation est obligatoire")
    @Valid
    private FirstRegistrationDate firstRegistrationDate;

    public TaxCalculationRequest() {
    }

    public Long getVariantId() {
        return variantId;
    }

    public void setVariantId(Long variantId) {
        this.variantId = variantId;
    }

    public Long getSubmissionId() {
        return submissionId;
    }

    public void setSubmissionId(Long submissionId) {
        this.submissionId = submissionId;
    }

    public Integer getFiscalHp() {
        return fiscalHp;
    }

    public void setFiscalHp(Integer fiscalHp) {
        this.fiscalHp = fiscalHp;
    }

    public Integer getPowerKw() {
        return powerKw;
    }

    public void setPowerKw(Integer powerKw) {
        this.powerKw = powerKw;
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

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public FirstRegistrationDate getFirstRegistrationDate() {
        return firstRegistrationDate;
    }

    public void setFirstRegistrationDate(FirstRegistrationDate firstRegistrationDate) {
        this.firstRegistrationDate = firstRegistrationDate;
    }

    /**
     * Verifie si des specs manuelles sont fournies (minimum: fiscalHp + fuel).
     */
    public boolean hasManualSpecs() {
        return fiscalHp != null && fuel != null;
    }

    /**
     * Valide qu'exactement un mode est utilise:
     * - variantId (vehicule catalogue)
     * - submissionId (soumission)
     * - specs manuelles (fiscalHp + fuel minimum)
     */
    @AssertTrue(message = "Vous devez fournir soit variantId, soit submissionId, soit les specs manuelles (fiscalHp + fuel minimum)")
    public boolean isValidVehicleReference() {
        int modes = 0;
        if (variantId != null) modes++;
        if (submissionId != null) modes++;
        if (hasManualSpecs()) modes++;
        return modes == 1;
    }
}
