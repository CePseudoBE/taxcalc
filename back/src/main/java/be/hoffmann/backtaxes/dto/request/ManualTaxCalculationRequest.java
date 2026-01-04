package be.hoffmann.backtaxes.dto.request;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * Requete pour calculer les taxes manuellement sans vehicule en base.
 *
 * Les champs requis dependent de la region:
 *
 * BRUXELLES (le plus simple):
 *   - TMC: fiscalHp OU powerKw (au moins un)
 *   - Annuelle: fiscalHp
 *
 * FLANDRE:
 *   - TMC: (fiscalHp OU powerKw), euroNorm, co2Wltp
 *   - Annuelle: fiscalHp
 *
 * WALLONIE (le plus complet):
 *   - TMC: powerKw, co2Wltp, mmaKg, fuel
 *   - Annuelle: fiscalHp
 */
public class ManualTaxCalculationRequest {

    @NotNull(message = "Region is required")
    private Region region;

    @NotNull(message = "La date de première immatriculation est obligatoire")
    @Valid
    private FirstRegistrationDate firstRegistrationDate;

    // --- Donnees vehicule (optionnelles selon region) ---

    private Integer powerKw;

    private Integer fiscalHp;

    private FuelType fuel;

    private EuroNorm euroNorm;

    private Integer co2Wltp;

    private Integer co2Nedc;

    private Integer mmaKg;

    // --- Infos pour soumission aux moderateurs ---

    private String brandName;

    private String modelName;

    private String variantName;

    private Integer yearStart;

    public ManualTaxCalculationRequest() {
    }

    /**
     * Valide les champs requis selon la region.
     * Retourne la liste des champs manquants pour la TMC.
     */
    public List<String> getMissingFieldsForTmc() {
        List<String> missing = new ArrayList<>();

        switch (region) {
            case brussels -> {
                // Bruxelles: fiscalHp OU powerKw suffit
                if (fiscalHp == null && powerKw == null) {
                    missing.add("fiscalHp ou powerKw (au moins un requis)");
                }
            }
            case flanders -> {
                // Flandre: (fiscalHp OU powerKw) + euroNorm + CO2
                if (fiscalHp == null && powerKw == null) {
                    missing.add("fiscalHp ou powerKw (au moins un requis)");
                }
                if (euroNorm == null) {
                    missing.add("euroNorm");
                }
                if (co2Wltp == null && co2Nedc == null) {
                    missing.add("co2Wltp ou co2Nedc (au moins un requis)");
                }
            }
            case wallonia -> {
                // Wallonie: powerKw + CO2 + poids + carburant
                if (powerKw == null) {
                    missing.add("powerKw");
                }
                if (co2Wltp == null && co2Nedc == null) {
                    missing.add("co2Wltp ou co2Nedc (au moins un requis)");
                }
                if (mmaKg == null) {
                    missing.add("mmaKg (masse maximale autorisee)");
                }
                if (fuel == null) {
                    missing.add("fuel (type de carburant)");
                }
            }
        }

        return missing;
    }

    /**
     * Valide les champs requis pour la taxe annuelle.
     */
    public List<String> getMissingFieldsForAnnual() {
        List<String> missing = new ArrayList<>();

        if (fiscalHp == null) {
            missing.add("fiscalHp");
        }

        return missing;
    }

    /**
     * Verifie si les donnees sont suffisantes pour calculer la TMC.
     */
    public boolean canCalculateTmc() {
        return getMissingFieldsForTmc().isEmpty();
    }

    /**
     * Verifie si les donnees sont suffisantes pour calculer la taxe annuelle.
     */
    public boolean canCalculateAnnual() {
        return getMissingFieldsForAnnual().isEmpty();
    }

    /**
     * Verifie si la soumission est complete (toutes les infos pour moderation).
     */
    public boolean isCompleteForSubmission() {
        return brandName != null && !brandName.isBlank()
                && modelName != null && !modelName.isBlank()
                && variantName != null && !variantName.isBlank()
                && yearStart != null
                && powerKw != null
                && fiscalHp != null
                && fuel != null
                && euroNorm != null;
    }

    // ==================== GETTERS & SETTERS ====================

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
}
