package be.hoffmann.backtaxes.dto.request;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Requete pour soumettre un nouveau vehicule au catalogue.
 */
public class VehicleSubmissionRequest {

    @NotBlank(message = "Brand name is required")
    @Size(max = 100, message = "Brand name must not exceed 100 characters")
    private String brandName;

    @NotBlank(message = "Model name is required")
    @Size(max = 100, message = "Model name must not exceed 100 characters")
    private String modelName;

    @NotBlank(message = "Variant name is required")
    @Size(max = 150, message = "Variant name must not exceed 150 characters")
    private String variantName;

    @NotNull(message = "Year start is required")
    private Integer yearStart;

    private Integer yearEnd;

    @NotNull(message = "Power in kW is required")
    @Positive(message = "Power must be positive")
    private Integer powerKw;

    @NotNull(message = "Fiscal horsepower is required")
    @Positive(message = "Fiscal horsepower must be positive")
    private Integer fiscalHp;

    @NotNull(message = "Fuel type is required")
    private FuelType fuel;

    @NotNull(message = "Euro norm is required")
    private EuroNorm euroNorm;

    private Integer co2Wltp;

    private Integer co2Nedc;

    private Integer displacementCc;

    private Integer mmaKg;

    private Boolean hasParticleFilter;

    public VehicleSubmissionRequest() {
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

    public Integer getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(Integer yearEnd) {
        this.yearEnd = yearEnd;
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

    public Integer getDisplacementCc() {
        return displacementCc;
    }

    public void setDisplacementCc(Integer displacementCc) {
        this.displacementCc = displacementCc;
    }

    public Integer getMmaKg() {
        return mmaKg;
    }

    public void setMmaKg(Integer mmaKg) {
        this.mmaKg = mmaKg;
    }

    public Boolean getHasParticleFilter() {
        return hasParticleFilter;
    }

    public void setHasParticleFilter(Boolean hasParticleFilter) {
        this.hasParticleFilter = hasParticleFilter;
    }
}
