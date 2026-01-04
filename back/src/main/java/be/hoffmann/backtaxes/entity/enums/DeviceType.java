package be.hoffmann.backtaxes.entity.enums;

/**
 * Types d'appareils utilises pour acceder a l'application.
 *
 * IMPORTANT: Les valeurs de l'enum Java doivent correspondre EXACTEMENT
 * aux valeurs du type PostgreSQL 'device_type'.
 */
public enum DeviceType {

    desktop,     // Ordinateur de bureau / laptop
    mobile,      // Smartphone
    tablet       // Tablette
}
