package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.AgeCoefficient;
import be.hoffmann.backtaxes.entity.TaxBracket;
import be.hoffmann.backtaxes.entity.TaxParameter;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.TaxType;
import be.hoffmann.backtaxes.repository.AgeCoefficientRepository;
import be.hoffmann.backtaxes.repository.TaxBracketRepository;
import be.hoffmann.backtaxes.repository.TaxExemptionRepository;
import be.hoffmann.backtaxes.repository.TaxParameterRepository;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

/**
 * Service pour acceder aux configurations de taxes.
 * Fournit des methodes simplifiees pour recuperer les baremes,
 * parametres et coefficients applicables.
 *
 * Les donnees sont cachees car elles changent rarement (1x par an max).
 */
@Service
@Transactional(readOnly = true)
public class TaxConfigService {

    private final TaxBracketRepository taxBracketRepository;
    private final TaxParameterRepository taxParameterRepository;
    private final AgeCoefficientRepository ageCoefficientRepository;
    private final TaxExemptionRepository taxExemptionRepository;

    public TaxConfigService(
            TaxBracketRepository taxBracketRepository,
            TaxParameterRepository taxParameterRepository,
            AgeCoefficientRepository ageCoefficientRepository,
            TaxExemptionRepository taxExemptionRepository) {
        this.taxBracketRepository = taxBracketRepository;
        this.taxParameterRepository = taxParameterRepository;
        this.ageCoefficientRepository = ageCoefficientRepository;
        this.taxExemptionRepository = taxExemptionRepository;
    }

    /**
     * Trouve le bareme applicable pour une valeur donnee.
     */
    @Cacheable(value = "taxBrackets",
            key = "#region.name() + '_' + #taxType.name() + '_' + #bracketKey + '_' + #value + '_' + #date.toString()")
    public Optional<TaxBracket> findBracket(Region region, TaxType taxType, String bracketKey, int value, LocalDate date) {
        return taxBracketRepository.findMatchingBracket(region, taxType, bracketKey, value, date);
    }

    /**
     * Recupere un parametre de taxe.
     */
    @Cacheable(value = "taxParameters",
            key = "#region.name() + '_' + #taxType.name() + '_' + #paramKey + '_' + #date.toString()")
    public Optional<BigDecimal> getParameter(Region region, TaxType taxType, String paramKey, LocalDate date) {
        return taxParameterRepository.findValidParameter(region, taxType, paramKey, date)
                .map(TaxParameter::getParamValue);
    }

    /**
     * Recupere un parametre de taxe avec valeur par defaut.
     */
    public BigDecimal getParameter(Region region, TaxType taxType, String paramKey, LocalDate date, BigDecimal defaultValue) {
        return getParameter(region, taxType, paramKey, date).orElse(defaultValue);
    }

    /**
     * Recupere le coefficient d'age pour un vehicule.
     */
    @Cacheable(value = "ageCoefficients",
            key = "#region.name() + '_' + #taxType.name() + '_' + #vehicleAgeYears + '_' + #date.toString()")
    public Optional<BigDecimal> getAgeCoefficient(Region region, TaxType taxType, int vehicleAgeYears, LocalDate date) {
        // Limiter l'age a un maximum raisonnable
        int age = Math.min(vehicleAgeYears, 15);
        return ageCoefficientRepository.findValidCoefficient(region, taxType, age, date)
                .map(AgeCoefficient::getCoefficient);
    }

    /**
     * Recupere le coefficient d'age avec valeur par defaut.
     */
    public BigDecimal getAgeCoefficient(Region region, TaxType taxType, int vehicleAgeYears, LocalDate date, BigDecimal defaultValue) {
        return getAgeCoefficient(region, taxType, vehicleAgeYears, date).orElse(defaultValue);
    }

    /**
     * Verifie si un vehicule est exonere de taxe.
     */
    @Cacheable(value = "taxExemptions",
            key = "#region.name() + '_' + #taxType.name() + '_' + #fuelType.name() + '_' + #date.toString()")
    public boolean isExempt(Region region, TaxType taxType, FuelType fuelType, LocalDate date) {
        String conditionKey = "fuel_" + fuelType.name();
        return taxExemptionRepository.isExempt(region, taxType, conditionKey, date);
    }

    /**
     * Verifie si un vehicule electrique ou hydrogene est exonere.
     */
    public boolean isZeroEmissionExempt(Region region, TaxType taxType, FuelType fuelType, LocalDate date) {
        if (fuelType == FuelType.electric || fuelType == FuelType.hydrogen) {
            return isExempt(region, taxType, fuelType, date);
        }
        return false;
    }

    /**
     * Recupere le montant minimum de taxe.
     */
    @Cacheable(value = "minMaxAmounts",
            key = "'min_' + #region.name() + '_' + #taxType.name() + '_' + #date.toString()")
    public BigDecimal getMinAmount(Region region, TaxType taxType, LocalDate date) {
        return getParameter(region, taxType, "min_amount", date, BigDecimal.ZERO);
    }

    /**
     * Recupere le montant maximum de taxe.
     */
    @Cacheable(value = "minMaxAmounts",
            key = "'max_' + #region.name() + '_' + #taxType.name() + '_' + #date.toString()")
    public Optional<BigDecimal> getMaxAmount(Region region, TaxType taxType, LocalDate date) {
        return getParameter(region, taxType, "max_amount", date);
    }

    /**
     * Convertit une cylindree en CV fiscaux selon les tranches officielles belges.
     * Utilise pour la taxe annuelle quand l'utilisateur connait la cylindree mais pas les CV fiscaux.
     *
     * @param region Region (les tranches sont identiques pour toutes les regions)
     * @param displacementCc Cylindree en cm3
     * @param date Date pour determiner les tranches valides
     * @return CV fiscaux correspondant a la tranche de cylindree
     */
    @Cacheable(value = "displacementToFiscalHp",
            key = "#region.name() + '_' + #displacementCc + '_' + #date.toString()")
    public Optional<Integer> getFiscalHpFromDisplacement(Region region, int displacementCc, LocalDate date) {
        return findBracket(region, TaxType.annual, "displacement_cc", displacementCc, date)
                .map(bracket -> bracket.getAmount().intValue());
    }
}
