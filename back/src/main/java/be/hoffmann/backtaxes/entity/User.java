package be.hoffmann.backtaxes.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilisateur de l'application.
 *
 * Note: "User" est un mot reserve dans certaines DB (pas PostgreSQL),
 * donc on specifie explicitement le nom de table "users".
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Mot de passe hashe (JAMAIS en clair!).
     * On utilisera BCrypt via Spring Security.
     */
    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    /**
     * Est-ce que l'utilisateur peut moderer les soumissions?
     * Par defaut: false (utilisateur normal).
     */
    @Column(name = "is_moderator")
    private Boolean isModerator = false;

    /**
     * Est-ce que l'utilisateur est administrateur?
     * Les admins peuvent gerer les utilisateurs et la configuration.
     * Par defaut: false.
     */
    @Column(name = "is_admin")
    private Boolean isAdmin = false;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // ==================== RELATIONS ====================

    /** Soumissions de vehicules faites par cet utilisateur */
    @OneToMany(mappedBy = "submitter", cascade = CascadeType.ALL)
    private List<VehicleSubmission> submissions = new ArrayList<>();

    /** Recherches sauvegardees par cet utilisateur */
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SavedSearch> savedSearches = new ArrayList<>();

    // ==================== CONSTRUCTEURS ====================

    public User() {
    }

    public User(String email, String passwordHash) {
        this.email = email;
        this.passwordHash = passwordHash;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public Boolean getIsModerator() {
        return isModerator;
    }

    public void setIsModerator(Boolean moderator) {
        isModerator = moderator;
    }

    public Boolean getIsAdmin() {
        return isAdmin;
    }

    public void setIsAdmin(Boolean admin) {
        isAdmin = admin;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public List<VehicleSubmission> getSubmissions() {
        return submissions;
    }

    public void setSubmissions(List<VehicleSubmission> submissions) {
        this.submissions = submissions;
    }

    public List<SavedSearch> getSavedSearches() {
        return savedSearches;
    }

    public void setSavedSearches(List<SavedSearch> savedSearches) {
        this.savedSearches = savedSearches;
    }
}
