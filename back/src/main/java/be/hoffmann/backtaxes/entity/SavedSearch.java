package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.Region;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;

/**
 * Recherche/calcul de taxe sauvegarde par un utilisateur.
 *
 * Permet a l'utilisateur de retrouver ses calculs precedents.
 *
 * PARTICULARITE: Contrainte XOR sur variant_id / submission_id
 *   - Soit on reference un Variant du catalogue officiel
 *   - Soit on reference une VehicleSubmission (vehicule en attente)
 *   - Jamais les deux, jamais aucun des deux
 *
 * Cette contrainte est geree en DB (CHECK constraint), pas en JPA.
 * En Java, on doit simplement s'assurer de respecter cette regle.
 */
@Entity
@Table(name = "saved_searches")
public class SavedSearch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** L'utilisateur qui a sauvegarde cette recherche */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Reference vers un vehicule du catalogue officiel.
     * Mutuellement exclusif avec submission.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private Variant variant;

    /**
     * Reference vers une soumission en cours.
     * Mutuellement exclusif avec variant.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id")
    private VehicleSubmission submission;

    /** Region pour laquelle le calcul a ete fait */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "region")
    private Region region;

    /**
     * Date de premiere immatriculation.
     *
     * LocalDate vs Instant:
     *   - LocalDate: juste une date (2024-03-15), sans heure ni timezone
     *   - Instant: moment precis (2024-03-15T10:30:00Z), avec timezone
     *
     * Pour une date d'immatriculation, on n'a pas besoin de l'heure.
     */
    @Column(name = "first_registration_date")
    private LocalDate firstRegistrationDate;

    /** Label personnalise donne par l'utilisateur */
    @Column(length = 100)
    private String label;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // ==================== CONSTRUCTEURS ====================

    public SavedSearch() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Variant getVariant() {
        return variant;
    }

    /**
     * Setter avec validation de la contrainte XOR.
     * Si on set un variant, on doit s'assurer que submission est null.
     */
    public void setVariant(Variant variant) {
        if (variant != null && this.submission != null) {
            throw new IllegalStateException(
                "Cannot set variant when submission is already set. Clear submission first."
            );
        }
        this.variant = variant;
    }

    public VehicleSubmission getSubmission() {
        return submission;
    }

    /**
     * Setter avec validation de la contrainte XOR.
     */
    public void setSubmission(VehicleSubmission submission) {
        if (submission != null && this.variant != null) {
            throw new IllegalStateException(
                "Cannot set submission when variant is already set. Clear variant first."
            );
        }
        this.submission = submission;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public LocalDate getFirstRegistrationDate() {
        return firstRegistrationDate;
    }

    public void setFirstRegistrationDate(LocalDate firstRegistrationDate) {
        this.firstRegistrationDate = firstRegistrationDate;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
