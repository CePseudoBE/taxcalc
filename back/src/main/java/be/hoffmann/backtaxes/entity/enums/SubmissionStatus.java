package be.hoffmann.backtaxes.entity.enums;

/**
 * Statut d'une soumission de vehicule par un utilisateur.
 *
 * Workflow: pending -> approved/rejected (par un moderateur)
 */
public enum SubmissionStatus {
    pending,    // En attente de moderation
    approved,   // Approuve - le vehicule sera ajoute au catalogue
    rejected    // Rejete - le vehicule ne sera pas ajoute
}
