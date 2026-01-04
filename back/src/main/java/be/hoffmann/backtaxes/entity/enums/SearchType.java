package be.hoffmann.backtaxes.entity.enums;

/**
 * Types de recherche effectuee par l'utilisateur.
 *
 * IMPORTANT: Les valeurs de l'enum Java doivent correspondre EXACTEMENT
 * aux valeurs du type PostgreSQL 'search_type'.
 */
public enum SearchType {

    browse,      // Navigation/exploration du catalogue
    filter,      // Recherche avec filtres (marque, modele, carburant...)
    calculate    // Calcul de taxe effectue
}
