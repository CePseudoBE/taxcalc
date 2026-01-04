package be.hoffmann.backtaxes.entity.enums;

/**
 * Types de classement pour les vehicules populaires.
 *
 * IMPORTANT: Les valeurs de l'enum Java doivent correspondre EXACTEMENT
 * aux valeurs du type PostgreSQL 'ranking_type'.
 */
public enum RankingType {

    most_searched,   // Vehicules les plus recherches
    highest_tax,     // Vehicules avec la taxe la plus elevee
    lowest_tax       // Vehicules avec la taxe la plus basse
}
