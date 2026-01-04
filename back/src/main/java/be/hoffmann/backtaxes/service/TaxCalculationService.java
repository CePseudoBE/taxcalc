package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.request.TaxCalculationRequest;
import be.hoffmann.backtaxes.dto.response.TaxCalculationResponse;
import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.Variant;
import be.hoffmann.backtaxes.entity.VehicleSubmission;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.repository.VehicleSubmissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;

/**
 * Service pour le calcul des taxes vehicules.
 * Supporte les trois regions belges avec leurs formules specifiques.
 */
@Service
@Transactional(readOnly = true)
public class TaxCalculationService {

    private static final Logger log = LoggerFactory.getLogger(TaxCalculationService.class);

    // Common BigDecimal constants for tax calculations
    private static final BigDecimal DEFAULT_CO2_REFERENCE_WALLONIA = BigDecimal.valueOf(136);
    private static final BigDecimal DEFAULT_MMA_REFERENCE = BigDecimal.valueOf(1838);
    private static final BigDecimal DEFAULT_ELECTRIC_MIN_COEFF = BigDecimal.valueOf(0.01);
    private static final BigDecimal DEFAULT_HYBRID_FACTOR = BigDecimal.valueOf(0.8);
    private static final BigDecimal DEFAULT_ELECTRIC_MIN_BRUSSELS = BigDecimal.valueOf(74.29);
    private static final BigDecimal DEFAULT_LPG_REDUCTION_BRUSSELS = BigDecimal.valueOf(298);
    private static final BigDecimal DEFAULT_FLANDERS_DIVISOR = BigDecimal.valueOf(246);
    private static final BigDecimal DEFAULT_FLANDERS_CONSTANT = BigDecimal.valueOf(4500);
    private static final BigDecimal DEFAULT_WLTP_Q_FACTOR = BigDecimal.valueOf(1.245);
    private static final BigDecimal DEFAULT_CO2_CORRECTION_X = BigDecimal.valueOf(63);
    private static final BigDecimal DEFAULT_LPG_FUEL_FACTOR = BigDecimal.valueOf(0.88);
    private static final BigDecimal DEFAULT_CNG_FUEL_FACTOR = BigDecimal.valueOf(0.93);
    private static final BigDecimal DEFAULT_MIN_BIV = BigDecimal.valueOf(500);
    private static final BigDecimal DEFAULT_LPG_SUPPLEMENT_PER_HP = BigDecimal.valueOf(99.16);
    private static final BigDecimal DEFAULT_CO2_REFERENCE_FLANDERS = BigDecimal.valueOf(149);
    private static final BigDecimal DEFAULT_CO2_CORRECTION_PERCENT = BigDecimal.valueOf(0.003);

    private final VariantService variantService;
    private final VehicleSubmissionRepository submissionRepository;
    private final TaxConfigService taxConfigService;

    public TaxCalculationService(
            VariantService variantService,
            VehicleSubmissionRepository submissionRepository,
            TaxConfigService taxConfigService) {
        this.variantService = variantService;
        this.submissionRepository = submissionRepository;
        this.taxConfigService = taxConfigService;
    }

    /**
     * Calcule les deux taxes (TMC et annuelle) pour une requete.
     */
    public TaxCalculationResult calculateBoth(TaxCalculationRequest request) {
        log.debug("Calculating both taxes for region {} with variantId={}, submissionId={}",
                request.getRegion(), request.getVariantId(), request.getSubmissionId());

        validateRequest(request);

        VehicleData vehicleData = getVehicleData(request);
        LocalDate calculationDate = request.getFirstRegistrationDate().toLocalDate();

        TaxCalculationResponse tmc = calculateTax(
                vehicleData, request.getRegion(), TaxType.tmc, calculationDate);

        TaxCalculationResponse annual = calculateTax(
                vehicleData, request.getRegion(), TaxType.annual, calculationDate);

        log.debug("Calculation complete: TMC={}, Annual={}", tmc.getAmount(), annual.getAmount());
        return new TaxCalculationResult(tmc, annual);
    }

    /**
     * Calcule uniquement la TMC pour une requete.
     * Plus efficace que calculateBoth() si seule la TMC est necessaire.
     */
    public TaxCalculationResponse calculateTmcOnly(TaxCalculationRequest request) {
        log.debug("Calculating TMC only for region {} with variantId={}, submissionId={}",
                request.getRegion(), request.getVariantId(), request.getSubmissionId());

        validateRequest(request);
        VehicleData vehicleData = getVehicleData(request);
        LocalDate calculationDate = request.getFirstRegistrationDate().toLocalDate();
        TaxCalculationResponse response = calculateTax(vehicleData, request.getRegion(), TaxType.tmc, calculationDate);

        log.debug("TMC calculation complete: amount={}, exempt={}", response.getAmount(), response.getIsExempt());
        return response;
    }

    /**
     * Calcule uniquement la taxe annuelle pour une requete.
     * Plus efficace que calculateBoth() si seule la taxe annuelle est necessaire.
     */
    public TaxCalculationResponse calculateAnnualOnly(TaxCalculationRequest request) {
        log.debug("Calculating annual tax only for region {} with variantId={}, submissionId={}",
                request.getRegion(), request.getVariantId(), request.getSubmissionId());

        validateRequest(request);
        VehicleData vehicleData = getVehicleData(request);
        LocalDate calculationDate = request.getFirstRegistrationDate().toLocalDate();
        TaxCalculationResponse response = calculateTax(vehicleData, request.getRegion(), TaxType.annual, calculationDate);

        log.debug("Annual tax calculation complete: amount={}, exempt={}", response.getAmount(), response.getIsExempt());
        return response;
    }

    /**
     * Calcule une taxe specifique.
     */
    public TaxCalculationResponse calculateTax(
            VehicleData vehicleData,
            Region region,
            TaxType taxType,
            LocalDate registrationDate) {

        TaxCalculationResponse response = new TaxCalculationResponse();
        response.setRegion(region);
        response.setTaxType(taxType);

        // Date de reference pour les baremes (aujourd'hui)
        // Note: on utilise la date courante pour chercher les baremes valides,
        // pas la date d'immatriculation qui sert a calculer l'age du vehicule
        LocalDate rateDate = LocalDate.now();

        // Verifier les exemptions (electrique, hydrogene)
        if (taxConfigService.isZeroEmissionExempt(region, taxType, vehicleData.fuel, rateDate)) {
            response.setAmount(BigDecimal.ZERO);
            response.setIsExempt(true);
            response.setExemptionReason("Zero emission vehicle (" + vehicleData.fuel + ")");
            return response;
        }

        response.setIsExempt(false);

        // Calculer l'age du vehicule par rapport a la date courante
        int vehicleAgeYears = calculateVehicleAge(registrationDate, rateDate);
        response.addBreakdownItem("vehicleAgeYears", vehicleAgeYears);

        // Calculer selon la region et le type de taxe
        BigDecimal amount;
        if (taxType == TaxType.tmc) {
            amount = calculateTmc(vehicleData, region, rateDate, vehicleAgeYears, registrationDate, response);
        } else {
            amount = calculateAnnual(vehicleData, region, rateDate, response);
        }

        // Appliquer les limites min/max
        // Note: le minimum ne s'applique pas si:
        // - le coefficient d'age est 0 (vehicule de 15+ ans)
        // - CNG exempt (Brussels TMC)
        Object ageCoefObj = response.getBreakdown().get("ageCoefficient");
        boolean ageExempt = ageCoefObj != null && BigDecimal.ZERO.compareTo(new BigDecimal(ageCoefObj.toString())) == 0;
        boolean cngExempt = Boolean.TRUE.equals(response.getBreakdown().get("cngExempt"));

        if (!ageExempt && !cngExempt) {
            BigDecimal minAmount = taxConfigService.getMinAmount(region, taxType, rateDate);
            if (amount.compareTo(minAmount) < 0) {
                amount = minAmount;
                response.addBreakdownItem("minAmountApplied", true);
            }
        }

        Optional<BigDecimal> maxAmountOpt = taxConfigService.getMaxAmount(region, taxType, rateDate);
        if (maxAmountOpt.isPresent()) {
            BigDecimal maxAmount = maxAmountOpt.get();
            if (amount.compareTo(maxAmount) > 0) {
                response.addBreakdownItem("maxAmountApplied", true);
                response.addBreakdownItem("originalAmount", amount);
                amount = maxAmount;
            }
        }

        response.setAmount(amount.setScale(2, RoundingMode.HALF_UP));
        return response;
    }

    /**
     * Calcule la TMC selon la region.
     */
    private BigDecimal calculateTmc(
            VehicleData vehicleData,
            Region region,
            LocalDate date,
            int vehicleAgeYears,
            LocalDate registrationDate,
            TaxCalculationResponse response) {

        BigDecimal baseAmount;
        BigDecimal ageCoefficient = taxConfigService.getAgeCoefficient(
                region, TaxType.tmc, vehicleAgeYears, date, BigDecimal.ONE);
        response.addBreakdownItem("ageCoefficient", ageCoefficient);

        baseAmount = switch (region) {
            case wallonia -> calculateWalloniaTmc(vehicleData, date, response);
            case brussels -> calculateBrusselsTmc(vehicleData, date, response);
            case flanders -> calculateFlandersTmc(vehicleData, date, registrationDate, response);
        };

        response.addBreakdownItem("baseAmount", baseAmount);

        // Appliquer le coefficient d'age
        // Note: Brussels electric a un tarif fixe, pas de coefficient d'age (sauf exemption 15+)
        BigDecimal finalAmount;
        boolean isElectricFixed = Boolean.TRUE.equals(response.getBreakdown().get("isElectricReduced"));
        if (isElectricFixed && ageCoefficient.compareTo(BigDecimal.ZERO) > 0) {
            // Tarif electrique fixe: pas de coefficient d'age (sauf si 0 = exempt)
            finalAmount = baseAmount;
            response.addBreakdownItem("electricFixedNoAgeCoef", true);
        } else {
            finalAmount = baseAmount.multiply(ageCoefficient);
        }
        response.addBreakdownItem("afterAgeCoefficient", finalAmount);

        return finalAmount;
    }

    /**
     * Calcule la TMC Wallonie (reforme 2025).
     * Formule officielle: TMC = MB × (CO2/X) × (MMA/Y) × C × coefficient_age
     * Ou MB = Montant de base (puissance kW)
     *    CO2 = emissions CO2 du vehicule
     *    X = 136 (WLTP) ou 115 (NEDC)
     *    MMA = Masse Maximale Autorisee
     *    Y = 1838 kg (reference)
     *    C = coefficient energie/carburant
     */
    private BigDecimal calculateWalloniaTmc(VehicleData vehicleData, LocalDate date, TaxCalculationResponse response) {
        // Montant de base selon la puissance (MB)
        BigDecimal baseAmount = taxConfigService.findBracket(
                        Region.wallonia, TaxType.tmc, "power_kw", vehicleData.powerKw, date)
                .map(TaxBracket::getAmount)
                .orElse(BigDecimal.ZERO);
        response.addBreakdownItem("powerKw", vehicleData.powerKw);
        response.addBreakdownItem("baseAmount", baseAmount);

        // Coefficient energie (C) - depend du type de carburant
        BigDecimal energyCoef = getWalloniaEnergyCoefficient(vehicleData, date);
        response.addBreakdownItem("energyCoefficient", energyCoef);

        // Facteur CO2 = CO2 / X (pour vehicules thermiques uniquement)
        BigDecimal co2Factor = BigDecimal.ONE;
        if (!isZeroEmissionFuel(vehicleData.fuel) && vehicleData.co2Wltp != null && vehicleData.co2Wltp > 0) {
            BigDecimal co2Reference = taxConfigService.getParameter(
                    Region.wallonia, TaxType.tmc, "co2_reference_wltp", date, BigDecimal.valueOf(136));
            co2Factor = BigDecimal.valueOf(vehicleData.co2Wltp)
                    .divide(co2Reference, 4, RoundingMode.HALF_UP);
            response.addBreakdownItem("co2Wltp", vehicleData.co2Wltp);
            response.addBreakdownItem("co2Reference", co2Reference);
            response.addBreakdownItem("co2Factor", co2Factor);
        }

        // Facteur MMA = MMA / Y
        BigDecimal mmaFactor = BigDecimal.ONE;
        if (vehicleData.mmaKg != null && vehicleData.mmaKg > 0) {
            BigDecimal mmaReference = taxConfigService.getParameter(
                    Region.wallonia, TaxType.tmc, "mma_reference", date, BigDecimal.valueOf(1838));
            mmaFactor = BigDecimal.valueOf(vehicleData.mmaKg)
                    .divide(mmaReference, 4, RoundingMode.HALF_UP);
            response.addBreakdownItem("mmaKg", vehicleData.mmaKg);
            response.addBreakdownItem("mmaReference", mmaReference);
            response.addBreakdownItem("mmaFactor", mmaFactor);
        }

        // Formule: MB × (CO2/X) × (MMA/Y) × C
        return baseAmount.multiply(co2Factor).multiply(mmaFactor).multiply(energyCoef);
    }

    /**
     * Retourne le coefficient energie pour Wallonie TMC.
     * - Electrique/Hydrogene: coefficient variable selon la puissance (0.01 a 0.26)
     * - Hybride: 0.8
     * - Thermique (essence, diesel, etc.): 1.0
     */
    private BigDecimal getWalloniaEnergyCoefficient(VehicleData vehicleData, LocalDate date) {
        FuelType fuel = vehicleData.fuel;

        // Vehicules electriques/hydrogene: coefficient selon puissance
        if (fuel == FuelType.electric || fuel == FuelType.hydrogen) {
            return taxConfigService.findBracket(
                            Region.wallonia, TaxType.tmc, "energy_electric_kw", vehicleData.powerKw, date)
                    .map(TaxBracket::getAmount)
                    .orElse(BigDecimal.valueOf(0.01));
        }

        // Vehicules hybrides
        if (isHybridFuel(fuel)) {
            return taxConfigService.getParameter(
                    Region.wallonia, TaxType.tmc, "energy_hybrid", date, BigDecimal.valueOf(0.8));
        }

        // Vehicules thermiques (essence, diesel, GPL, CNG, etc.)
        return taxConfigService.getParameter(
                Region.wallonia, TaxType.tmc, "energy_thermal", date, BigDecimal.ONE);
    }

    /**
     * Verifie si le carburant est zero emission.
     */
    private boolean isZeroEmissionFuel(FuelType fuel) {
        return fuel == FuelType.electric || fuel == FuelType.hydrogen;
    }

    /**
     * Verifie si le carburant est hybride.
     */
    private boolean isHybridFuel(FuelType fuel) {
        return fuel == FuelType.hybrid_petrol || fuel == FuelType.hybrid_diesel
                || fuel == FuelType.plug_in_hybrid_petrol || fuel == FuelType.plug_in_hybrid_diesel;
    }

    /**
     * Calcule la TMC Bruxelles.
     * Formule: Max(montant_CV, montant_kW) × Coef. age - Reduction LPG
     * On compare les MONTANTS (pas les valeurs CV/kW) et on prend le plus eleve.
     * Electrique/Hydrogene: tarif reduit fixe de 74.29€
     */
    private BigDecimal calculateBrusselsTmc(VehicleData vehicleData, LocalDate date, TaxCalculationResponse response) {
        // Vehicules electriques/hydrogene: tarif reduit fixe (pas exempt, mais minimum garanti)
        if (isZeroEmissionFuel(vehicleData.fuel)) {
            BigDecimal electricAmount = taxConfigService.getParameter(
                    Region.brussels, TaxType.tmc, "electric_min_amount", date, BigDecimal.valueOf(74.29));
            response.addBreakdownItem("electricFixedAmount", electricAmount);
            response.addBreakdownItem("isElectricReduced", true);
            return electricAmount;
        }

        // Montant selon CV fiscaux
        BigDecimal amountByFiscalHp = taxConfigService.findBracket(
                        Region.brussels, TaxType.tmc, "fiscal_hp", vehicleData.fiscalHp, date)
                .map(TaxBracket::getAmount)
                .orElse(BigDecimal.ZERO);
        response.addBreakdownItem("fiscalHp", vehicleData.fiscalHp);
        response.addBreakdownItem("amountByFiscalHp", amountByFiscalHp);

        // Montant selon puissance kW
        BigDecimal amountByPowerKw = taxConfigService.findBracket(
                        Region.brussels, TaxType.tmc, "power_kw", vehicleData.powerKw, date)
                .map(TaxBracket::getAmount)
                .orElse(BigDecimal.ZERO);
        response.addBreakdownItem("powerKw", vehicleData.powerKw);
        response.addBreakdownItem("amountByPowerKw", amountByPowerKw);

        // Prendre le montant le plus eleve
        BigDecimal baseAmount = amountByFiscalHp.max(amountByPowerKw);
        response.addBreakdownItem("baseAmount", baseAmount);
        response.addBreakdownItem("usedCriteria", amountByFiscalHp.compareTo(amountByPowerKw) >= 0 ? "fiscal_hp" : "power_kw");

        // Reduction LPG
        if (vehicleData.fuel == FuelType.lpg) {
            BigDecimal lpgReduction = taxConfigService.getParameter(
                    Region.brussels, TaxType.tmc, "lpg_reduction", date, BigDecimal.valueOf(298));
            baseAmount = baseAmount.subtract(lpgReduction);
            response.addBreakdownItem("lpgReduction", lpgReduction);
        }

        // CNG est exempt (0€)
        if (vehicleData.fuel == FuelType.cng) {
            response.addBreakdownItem("cngExempt", true);
            return BigDecimal.ZERO;
        }

        return baseAmount.max(BigDecimal.ZERO);
    }

    /**
     * Calcule la TMC Flandre (BIV).
     *
     * DEUX FORMULES selon la date de premiere immatriculation:
     * - Pre-2021 (NEDC): BIV = (((CO2 × f + x) / 246)^6 × 4500 + c) × LC
     * - 2021+ (WLTP):    BIV = (((CO2 × f × q) / 246)^6 × 4500 + c) × LC
     *
     * Ou:
     *   CO2 = emissions CO2 (g/km)
     *   f = facteur carburant (LPG: 0.88, CNG: 0.93, bicarburation CNG: 0.744, autres: 1.0)
     *   x = terme de correction CO2 additif (NEDC, +4.5g/an depuis 2013)
     *   q = facteur multiplicatif WLTP (1.07 en 2021, +0.035/an)
     *   c = luchtcomponent (selon euronorm et type de carburant)
     *   LC = coefficient d'age (applique en amont dans calculateTmc)
     *
     * Note: LC est applique APRES cette methode dans calculateTmc()
     */
    private BigDecimal calculateFlandersTmc(VehicleData vehicleData, LocalDate date, LocalDate registrationDate, TaxCalculationResponse response) {
        // Vehicules electriques/hydrogene: montant fixe a partir de 2026
        if (isZeroEmissionFuel(vehicleData.fuel)) {
            Optional<BigDecimal> electricFixed = taxConfigService.getParameter(
                    Region.flanders, TaxType.tmc, "electric_fixed_amount", date);
            if (electricFixed.isPresent()) {
                response.addBreakdownItem("electricFixedAmount", electricFixed.get());
                response.addBreakdownItem("isElectricReduced", true);
                return electricFixed.get();
            }
            // Avant 2026: exempt (gere par isZeroEmissionExempt dans calculateTax)
            return BigDecimal.ZERO;
        }

        // Determiner si le vehicule est NEDC (pre-2021) ou WLTP (2021+)
        LocalDate wltpStartDate = LocalDate.of(2021, 1, 1);
        boolean isWltp = registrationDate != null && !registrationDate.isBefore(wltpStartDate);
        response.addBreakdownItem("formulaType", isWltp ? "WLTP" : "NEDC");

        // CO2 emissions (utiliser NEDC si disponible pour vehicules pre-2021)
        int co2;
        if (isWltp) {
            co2 = vehicleData.co2Wltp != null && vehicleData.co2Wltp > 0
                    ? vehicleData.co2Wltp
                    : getDefaultCo2(vehicleData);
            response.addBreakdownItem("co2Wltp", co2);
        } else {
            // Pour NEDC, preferer co2Nedc si disponible
            co2 = vehicleData.co2Nedc != null && vehicleData.co2Nedc > 0
                    ? vehicleData.co2Nedc
                    : (vehicleData.co2Wltp != null && vehicleData.co2Wltp > 0
                            ? vehicleData.co2Wltp
                            : getDefaultCo2(vehicleData));
            response.addBreakdownItem("co2Nedc", co2);
        }

        // f = facteur carburant
        BigDecimal f = getFlandersFuelFactor(vehicleData.fuel, date);
        response.addBreakdownItem("fuelFactor_f", f);

        // Diviseur (246)
        BigDecimal divisor = taxConfigService.getParameter(
                Region.flanders, TaxType.tmc, "formula_divisor", date, BigDecimal.valueOf(246));

        // Constante (4500)
        BigDecimal constant = taxConfigService.getParameter(
                Region.flanders, TaxType.tmc, "formula_constant", date, BigDecimal.valueOf(4500));

        // c = luchtcomponent (selon euronorm et carburant)
        BigDecimal c = getFlandersLuchtcomponent(vehicleData, date);
        response.addBreakdownItem("luchtcomponent_c", c);
        response.addBreakdownItem("euroNorm", vehicleData.euroNorm);

        // Calculer selon la formule appropriee
        BigDecimal step1;
        if (isWltp) {
            // Formule WLTP: CO2 × f × q
            BigDecimal q = taxConfigService.getParameter(
                    Region.flanders, TaxType.tmc, "wltp_q_factor", date, BigDecimal.valueOf(1.245));
            response.addBreakdownItem("wltp_q_factor", q);
            step1 = BigDecimal.valueOf(co2).multiply(f).multiply(q);
            response.addBreakdownItem("step1_co2_f_q", step1);
        } else {
            // Formule NEDC: CO2 × f + x
            BigDecimal x = taxConfigService.getParameter(
                    Region.flanders, TaxType.tmc, "co2_correction_x", date, BigDecimal.valueOf(63));
            response.addBreakdownItem("co2Correction_x", x);
            step1 = BigDecimal.valueOf(co2).multiply(f).add(x);
            response.addBreakdownItem("step1_co2_f_x", step1);
        }

        // Etape 2: / 246
        BigDecimal step2 = step1.divide(divisor, 10, RoundingMode.HALF_UP);
        response.addBreakdownItem("step2_divided", step2);

        // Etape 3: ^6
        BigDecimal step3 = step2.pow(6);
        response.addBreakdownItem("step3_power6", step3);

        // Etape 4: × 4500
        BigDecimal step4 = step3.multiply(constant);
        response.addBreakdownItem("step4_times4500", step4);

        // Etape 5: + c (luchtcomponent)
        BigDecimal result = step4.add(c);
        response.addBreakdownItem("step5_plus_luchtcomponent", result);

        // Note: le coefficient d'age (LC) est applique dans calculateTmc()
        return result;
    }

    /**
     * Retourne le facteur carburant (f) pour la formule BIV Flandre.
     */
    private BigDecimal getFlandersFuelFactor(FuelType fuel, LocalDate date) {
        if (fuel == FuelType.lpg) {
            return taxConfigService.getParameter(
                    Region.flanders, TaxType.tmc, "fuel_factor_lpg", date, BigDecimal.valueOf(0.88));
        }
        if (fuel == FuelType.cng) {
            return taxConfigService.getParameter(
                    Region.flanders, TaxType.tmc, "fuel_factor_cng", date, BigDecimal.valueOf(0.93));
        }
        return taxConfigService.getParameter(
                Region.flanders, TaxType.tmc, "fuel_factor_default", date, BigDecimal.ONE);
    }

    /**
     * Retourne le luchtcomponent (c) pour la formule BIV Flandre.
     * Depend du type de carburant (diesel vs essence/LPG/CNG) et de la norme Euro.
     */
    private BigDecimal getFlandersLuchtcomponent(VehicleData vehicleData, LocalDate date) {
        int euroNormValue = getEuroNormNumericValue(vehicleData.euroNorm);

        // Diesel a un luchtcomponent plus eleve
        String bracketKey = (vehicleData.fuel == FuelType.diesel || vehicleData.fuel == FuelType.hybrid_diesel
                || vehicleData.fuel == FuelType.plug_in_hybrid_diesel)
                ? "luchtcomponent_diesel"
                : "luchtcomponent_petrol";

        return taxConfigService.findBracket(
                        Region.flanders, TaxType.tmc, bracketKey, euroNormValue, date)
                .map(TaxBracket::getAmount)
                .orElse(BigDecimal.valueOf(500)); // Valeur par defaut raisonnable
    }

    /**
     * Retourne une valeur CO2 par defaut si non disponible.
     * Basee sur des estimations selon le type de carburant et la puissance.
     */
    private int getDefaultCo2(VehicleData vehicleData) {
        // Estimation basee sur des moyennes du marche
        if (vehicleData.fuel == FuelType.diesel || vehicleData.fuel == FuelType.hybrid_diesel) {
            return 120 + (vehicleData.powerKw / 2);
        }
        // Essence et autres
        return 130 + (vehicleData.powerKw / 2);
    }

    /**
     * Convertit un EuroNorm en valeur numerique pour les brackets Flandre.
     */
    private int getEuroNormNumericValue(be.hoffmann.backtaxes.entity.enums.EuroNorm euroNorm) {
        if (euroNorm == null) return 6; // Default to Euro 6
        return switch (euroNorm) {
            case euro_1 -> 1;
            case euro_2 -> 2;
            case euro_3 -> 3;
            case euro_4 -> 4;
            case euro_5 -> 5;
            case euro_6, euro_6d_temp, euro_6d -> 6;
            case euro_7 -> 7;
        };
    }

    /**
     * Calcule la taxe annuelle.
     */
    private BigDecimal calculateAnnual(
            VehicleData vehicleData,
            Region region,
            LocalDate date,
            TaxCalculationResponse response) {

        // Montant de base selon CV fiscaux
        BigDecimal baseAmount = taxConfigService.findBracket(
                        region, TaxType.annual, "fiscal_hp", vehicleData.fiscalHp, date)
                .map(TaxBracket::getAmount)
                .orElse(BigDecimal.ZERO);
        response.addBreakdownItem("baseAmount", baseAmount);
        response.addBreakdownItem("fiscalHp", vehicleData.fiscalHp);

        // Supplement LPG
        if (vehicleData.fuel == FuelType.lpg) {
            BigDecimal lpgSupplement = taxConfigService.getParameter(
                            region, TaxType.annual, "lpg_supplement_per_hp", date, BigDecimal.valueOf(99.16))
                    .multiply(BigDecimal.valueOf(vehicleData.fiscalHp));
            baseAmount = baseAmount.add(lpgSupplement);
            response.addBreakdownItem("lpgSupplement", lpgSupplement);
        }

        // Bonus/Malus CO2 pour la Flandre
        if (region == Region.flanders && vehicleData.co2Wltp != null && vehicleData.co2Wltp > 0) {
            BigDecimal co2Reference = taxConfigService.getParameter(
                    Region.flanders, TaxType.annual, "co2_reference_wltp", date, BigDecimal.valueOf(149));
            BigDecimal co2Diff = BigDecimal.valueOf(vehicleData.co2Wltp).subtract(co2Reference);
            BigDecimal correctionPercent = taxConfigService.getParameter(
                    Region.flanders, TaxType.annual, "co2_correction_percent", date, BigDecimal.valueOf(0.003));
            BigDecimal co2Adjustment = baseAmount.multiply(co2Diff).multiply(correctionPercent);
            baseAmount = baseAmount.add(co2Adjustment);
            response.addBreakdownItem("co2Adjustment", co2Adjustment);
        }

        return baseAmount.max(BigDecimal.ZERO);
    }

    /**
     * Calcule l'age du vehicule en annees.
     * Pour la TMC, l'age est calcule par rapport a la date de calcul (pas aujourd'hui).
     */
    private int calculateVehicleAge(LocalDate registrationDate, LocalDate referenceDate) {
        if (registrationDate == null) {
            return 0;
        }
        LocalDate refDate = referenceDate != null ? referenceDate : LocalDate.now();
        int years = Period.between(registrationDate, refDate).getYears();
        return Math.max(0, years); // Ne pas retourner d'age negatif pour les vehicules neufs
    }

    /**
     * Valide la requete de calcul.
     */
    private void validateRequest(TaxCalculationRequest request) {
        if (!request.hasValidVehicleReference()) {
            throw new ValidationException("Either variantId or submissionId must be provided, but not both");
        }
        if (request.getRegion() == null) {
            throw new ValidationException("region", "Region is required");
        }
        if (request.getFirstRegistrationDate() == null || !request.getFirstRegistrationDate().isValid()) {
            throw new ValidationException("firstRegistrationDate", "La date de première immatriculation est obligatoire");
        }
    }

    /**
     * Recupere les donnees du vehicule depuis une variante ou soumission.
     */
    private VehicleData getVehicleData(TaxCalculationRequest request) {
        if (request.getVariantId() != null) {
            Variant variant = variantService.findById(request.getVariantId());
            return VehicleData.fromVariant(variant);
        } else {
            VehicleSubmission submission = submissionRepository.findById(request.getSubmissionId())
                    .orElseThrow(() -> new ResourceNotFoundException("VehicleSubmission", "id", request.getSubmissionId()));
            return VehicleData.fromSubmission(submission);
        }
    }

    /**
     * Donnees vehicule normalisees pour le calcul.
     */
    public static class VehicleData {
        public final Integer powerKw;
        public final Integer fiscalHp;
        public final FuelType fuel;
        public final be.hoffmann.backtaxes.entity.enums.EuroNorm euroNorm;
        public final Integer co2Wltp;
        public final Integer co2Nedc;
        public final Integer mmaKg;

        public VehicleData(Integer powerKw, Integer fiscalHp, FuelType fuel,
                           be.hoffmann.backtaxes.entity.enums.EuroNorm euroNorm,
                           Integer co2Wltp, Integer co2Nedc, Integer mmaKg) {
            this.powerKw = powerKw != null ? powerKw : 0;
            this.fiscalHp = fiscalHp != null ? fiscalHp : 0;
            this.fuel = fuel;
            this.euroNorm = euroNorm;
            this.co2Wltp = co2Wltp;
            this.co2Nedc = co2Nedc;
            this.mmaKg = mmaKg;
        }

        public static VehicleData fromVariant(Variant v) {
            return new VehicleData(
                    v.getPowerKw(), v.getFiscalHp(), v.getFuel(),
                    v.getEuroNorm(), v.getCo2Wltp(), v.getCo2Nedc(), v.getMmaKg());
        }

        public static VehicleData fromSubmission(VehicleSubmission s) {
            return new VehicleData(
                    s.getPowerKw(), s.getFiscalHp(), s.getFuel(),
                    s.getEuroNorm(), s.getCo2Wltp(), s.getCo2Nedc(), s.getMmaKg());
        }
    }

    /**
     * Resultat contenant les deux types de taxe.
     */
    public static class TaxCalculationResult {
        private final TaxCalculationResponse tmc;
        private final TaxCalculationResponse annual;

        public TaxCalculationResult(TaxCalculationResponse tmc, TaxCalculationResponse annual) {
            this.tmc = tmc;
            this.annual = annual;
        }

        public TaxCalculationResponse getTmc() {
            return tmc;
        }

        public TaxCalculationResponse getAnnual() {
            return annual;
        }
    }
}
