package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.request.ManualTaxCalculationRequest;
import be.hoffmann.backtaxes.dto.response.ManualTaxCalculationResponse;
import be.hoffmann.backtaxes.dto.response.ManualTaxCalculationResponse.*;
import be.hoffmann.backtaxes.dto.response.TaxCalculationResponse;
import be.hoffmann.backtaxes.entity.PendingCalculation;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.repository.PendingCalculationRepository;
import be.hoffmann.backtaxes.service.TaxCalculationService.VehicleData;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Service pour le calcul manuel des taxes.
 * Permet aux utilisateurs de calculer leurs taxes avec des donnees partielles.
 */
@Service
@Transactional
public class ManualTaxCalculationService {

    private final TaxCalculationService taxCalculationService;
    private final TaxConfigService taxConfigService;
    private final PendingCalculationRepository pendingCalculationRepository;

    public ManualTaxCalculationService(
            TaxCalculationService taxCalculationService,
            TaxConfigService taxConfigService,
            PendingCalculationRepository pendingCalculationRepository) {
        this.taxCalculationService = taxCalculationService;
        this.taxConfigService = taxConfigService;
        this.pendingCalculationRepository = pendingCalculationRepository;
    }

    /**
     * Calcule les taxes avec les donnees fournies.
     */
    public ManualTaxCalculationResponse calculate(
            ManualTaxCalculationRequest request,
            boolean saveForModeration,
            UUID sessionId) {

        ManualTaxCalculationResponse response = new ManualTaxCalculationResponse();
        response.setRegion(request.getRegion());

        // Determiner les champs manquants
        List<String> missingForTmc = request.getMissingFieldsForTmc();
        List<String> missingForAnnual = request.getMissingFieldsForAnnual();

        // Configurer les requirements avec statut provided
        response.setRequirements(buildRequirements(request));

        LocalDate registrationDate = request.getFirstRegistrationDate().toLocalDate();

        VehicleData vehicleData = createVehicleData(request);

        // Calculer TMC
        TaxResult tmcResult = new TaxResult();
        if (missingForTmc.isEmpty()) {
            try {
                TaxCalculationResponse tmcResponse = taxCalculationService.calculateTax(
                        vehicleData, request.getRegion(), TaxType.tmc, registrationDate);
                tmcResult.fromTaxCalculationResponse(tmcResponse);
            } catch (Exception e) {
                tmcResult.setCalculable(false);
                tmcResult.setMissingFields(List.of("Error: " + e.getMessage()));
            }
        } else {
            tmcResult.setCalculable(false);
            tmcResult.setMissingFields(missingForTmc);
        }
        response.getTaxes().setTmc(tmcResult);

        // Calculer taxe annuelle
        TaxResult annualResult = new TaxResult();
        if (missingForAnnual.isEmpty()) {
            try {
                TaxCalculationResponse annualResponse = taxCalculationService.calculateTax(
                        vehicleData, request.getRegion(), TaxType.annual, registrationDate);
                annualResult.fromTaxCalculationResponse(annualResponse);
            } catch (Exception e) {
                annualResult.setCalculable(false);
                annualResult.setMissingFields(List.of("Error: " + e.getMessage()));
            }
        } else {
            annualResult.setCalculable(false);
            annualResult.setMissingFields(missingForAnnual);
        }
        response.getTaxes().setAnnual(annualResult);

        // Calculer le statut global
        response.computeStatus();

        // Message selon le statut
        response.setMessage(buildMessage(response.getStatus(), request.getRegion()));

        // Sauvegarder pour moderation si demande
        if (saveForModeration && response.getStatus() != Status.complete) {
            PendingCalculation pending = savePendingCalculation(request, response, sessionId);
            response.setPendingSubmission(new PendingInfo(
                    pending.getId(),
                    "pending",
                    "A moderator will complete the missing information."
            ));
        }

        return response;
    }

    /**
     * Construit les requirements avec indication des champs fournis.
     */
    private RegionRequirements buildRequirements(ManualTaxCalculationRequest request) {
        RegionRequirements req = new RegionRequirements();

        boolean hasFiscalHpOrDisplacement = request.getFiscalHp() != null || request.getDisplacementCc() != null;

        switch (request.getRegion()) {
            case brussels -> {
                req.setTmc(List.of(
                        new FieldRequirement("fiscalHp", "Fiscal HP", request.getFiscalHp() != null),
                        new FieldRequirement("powerKw", "Power in kW (alternative)", request.getPowerKw() != null)
                ));
                req.setAnnual(List.of(
                        new FieldRequirement("fiscalHp", "Fiscal HP or Displacement", hasFiscalHpOrDisplacement)
                ));
            }
            case flanders -> {
                req.setTmc(List.of(
                        new FieldRequirement("fiscalHp", "Fiscal HP", request.getFiscalHp() != null),
                        new FieldRequirement("powerKw", "Power in kW (alternative)", request.getPowerKw() != null),
                        new FieldRequirement("euroNorm", "Euro norm", request.getEuroNorm() != null),
                        new FieldRequirement("co2Wltp", "CO2 emissions WLTP (g/km)", request.getCo2Wltp() != null)
                ));
                req.setAnnual(List.of(
                        new FieldRequirement("fiscalHp", "Fiscal HP or Displacement", hasFiscalHpOrDisplacement)
                ));
            }
            case wallonia -> {
                req.setTmc(List.of(
                        new FieldRequirement("powerKw", "Power in kW", request.getPowerKw() != null),
                        new FieldRequirement("fuel", "Fuel type", request.getFuel() != null),
                        new FieldRequirement("co2Wltp", "CO2 emissions WLTP (g/km)", request.getCo2Wltp() != null),
                        new FieldRequirement("mmaKg", "Maximum authorized mass (kg)", request.getMmaKg() != null)
                ));
                req.setAnnual(List.of(
                        new FieldRequirement("fiscalHp", "Fiscal HP or Displacement", hasFiscalHpOrDisplacement)
                ));
            }
        }

        return req;
    }

    /**
     * Construit le message selon le statut.
     */
    private String buildMessage(Status status, Region region) {
        return switch (status) {
            case complete -> "Calculation completed for " + getRegionName(region) + ".";
            case partial -> "Partial calculation. Some data is missing for a complete calculation.";
            case insufficient -> "Insufficient data. Please provide the required fields for " + getRegionName(region) + ".";
        };
    }

    private String getRegionName(Region region) {
        return switch (region) {
            case wallonia -> "Wallonia";
            case brussels -> "Brussels";
            case flanders -> "Flanders";
        };
    }

    private VehicleData createVehicleData(ManualTaxCalculationRequest request) {
        // Si fiscalHp n'est pas fourni mais displacementCc l'est, convertir via les tranches
        Integer effectiveFiscalHp = request.getFiscalHp();
        if (effectiveFiscalHp == null && request.getDisplacementCc() != null) {
            effectiveFiscalHp = taxConfigService.getFiscalHpFromDisplacement(
                    request.getRegion(),
                    request.getDisplacementCc(),
                    LocalDate.now()
            ).orElse(4); // Minimum 4 CV si pas de tranche trouvee
        }
        return new VehicleData(
                request.getPowerKw(),
                effectiveFiscalHp,
                request.getFuel(),
                request.getEuroNorm(),
                request.getCo2Wltp(),
                request.getCo2Nedc(),
                request.getMmaKg()
        );
    }

    private PendingCalculation savePendingCalculation(
            ManualTaxCalculationRequest request,
            ManualTaxCalculationResponse response,
            UUID sessionId) {

        PendingCalculation pending = new PendingCalculation();
        pending.setSessionId(sessionId);
        pending.setRegion(request.getRegion());
        pending.setStatus(PendingCalculation.Status.pending);

        // Donnees vehicule
        pending.setBrandName(request.getBrandName());
        pending.setModelName(request.getModelName());
        pending.setVariantName(request.getVariantName());
        pending.setYearStart(request.getYearStart());
        pending.setPowerKw(request.getPowerKw());
        pending.setFiscalHp(request.getFiscalHp());
        pending.setFuel(request.getFuel());
        pending.setEuroNorm(request.getEuroNorm());
        pending.setCo2Wltp(request.getCo2Wltp());
        pending.setCo2Nedc(request.getCo2Nedc());
        pending.setMmaKg(request.getMmaKg());

        // Resultats du calcul partiel
        TaxResult tmc = response.getTaxes().getTmc();
        TaxResult annual = response.getTaxes().getAnnual();
        pending.setTmcCalculable(tmc != null && tmc.isCalculable());
        pending.setAnnualCalculable(annual != null && annual.isCalculable());
        if (tmc != null && tmc.getAmount() != null) {
            pending.setCalculatedTmc(tmc.getAmount());
        }
        if (annual != null && annual.getAmount() != null) {
            pending.setCalculatedAnnual(annual.getAmount());
        }

        return pendingCalculationRepository.save(pending);
    }

    public List<PendingCalculation> getPendingCalculations() {
        return pendingCalculationRepository.findByStatus(PendingCalculation.Status.pending);
    }

    public List<PendingCalculation> getPendingCalculationsByRegion(Region region) {
        return pendingCalculationRepository.findByStatusAndRegion(
                PendingCalculation.Status.pending, region);
    }
}
