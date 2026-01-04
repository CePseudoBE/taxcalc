package be.hoffmann.backtaxes.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Représente la date de première immatriculation au format MM/YYYY.
 *
 * Si l'utilisateur ne connaît pas le mois exact, il peut mettre monthUnknown=true
 * et on utilisera janvier par défaut (cas le plus défavorable pour l'utilisateur = taxe plus haute).
 */
public class FirstRegistrationDate {

    @NotNull(message = "L'année est obligatoire")
    @Min(value = 1900, message = "L'année doit être supérieure à 1900")
    @Max(value = 2100, message = "L'année doit être inférieure à 2100")
    private Integer year;

    @Min(value = 1, message = "Le mois doit être entre 1 et 12")
    @Max(value = 12, message = "Le mois doit être entre 1 et 12")
    private Integer month;

    /**
     * Si true, le mois n'est pas connu et on utilise janvier par défaut.
     * Dans ce cas, le champ month est ignoré.
     */
    private Boolean monthUnknown;

    public FirstRegistrationDate() {
    }

    public FirstRegistrationDate(Integer year, Integer month) {
        this.year = year;
        this.month = month;
        this.monthUnknown = false;
    }

    public FirstRegistrationDate(Integer year, boolean monthUnknown) {
        this.year = year;
        this.monthUnknown = monthUnknown;
        if (monthUnknown) {
            this.month = null;
        }
    }

    /**
     * Retourne la date effective (1er du mois).
     * Si le mois est inconnu, retourne le 1er janvier de l'année.
     */
    public LocalDate toLocalDate() {
        int effectiveMonth = (monthUnknown != null && monthUnknown) ? 1 : (month != null ? month : 1);
        return LocalDate.of(year, effectiveMonth, 1);
    }

    /**
     * Retourne le YearMonth effectif.
     */
    public YearMonth toYearMonth() {
        int effectiveMonth = (monthUnknown != null && monthUnknown) ? 1 : (month != null ? month : 1);
        return YearMonth.of(year, effectiveMonth);
    }

    /**
     * Valide que la date est cohérente.
     */
    public boolean isValid() {
        if (year == null) {
            return false;
        }
        if (monthUnknown != null && monthUnknown) {
            return true;
        }
        return month != null && month >= 1 && month <= 12;
    }

    /**
     * Retourne une représentation string au format MM/YYYY ou YYYY si mois inconnu.
     */
    public String toDisplayString() {
        if (monthUnknown != null && monthUnknown) {
            return year.toString() + " (mois inconnu)";
        }
        return String.format("%02d/%d", month, year);
    }

    // Getters & Setters

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public Integer getMonth() {
        return month;
    }

    public void setMonth(Integer month) {
        this.month = month;
    }

    public Boolean getMonthUnknown() {
        return monthUnknown;
    }

    public void setMonthUnknown(Boolean monthUnknown) {
        this.monthUnknown = monthUnknown;
    }
}
