package be.hoffmann.backtaxes.entity.enums;

/**
 * Types de carburant/energie pour les vehicules.
 *
 * IMPORTANT: Les valeurs de l'enum Java doivent correspondre EXACTEMENT
 * aux valeurs du type PostgreSQL 'fuel_type'.
 *
 * En JPA, quand on utilise @Enumerated(EnumType.STRING), le nom de l'enum
 * est stocke tel quel dans la base de donnees.
 */
public enum FuelType {

    // Carburants classiques
    petrol,              // Essence
    diesel,              // Diesel
    lpg,                 // GPL (Gaz de Petrole Liquefie)
    cng,                 // GNC (Gaz Naturel Comprime)

    // Hybrides (moteur thermique + electrique)
    hybrid_petrol,       // Hybride essence (non rechargeable)
    hybrid_diesel,       // Hybride diesel (non rechargeable)

    // Hybrides rechargeables (PHEV)
    plug_in_hybrid_petrol,   // Hybride rechargeable essence
    plug_in_hybrid_diesel,   // Hybride rechargeable diesel

    // Zero emission
    electric,            // 100% electrique
    hydrogen             // Pile a combustible hydrogene
}
