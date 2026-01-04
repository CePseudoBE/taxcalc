package be.hoffmann.backtaxes.entity.enums;

/**
 * Types de rapports analytics generes.
 *
 * IMPORTANT: Les valeurs de l'enum Java doivent correspondre EXACTEMENT
 * aux valeurs du type PostgreSQL 'report_type'.
 */
public enum ReportType {

    market_trends,       // Tendances du marche (top marques, modeles)
    tax_analysis,        // Analyse fiscale (repartition taxes, comparaisons)
    user_behavior,       // Comportement utilisateur (devices, sources, conversions)
    regional_comparison, // Comparaison entre regions
    monthly_summary,     // Resume mensuel global
    data_retention       // Rapport pre-purge des donnees brutes
}
