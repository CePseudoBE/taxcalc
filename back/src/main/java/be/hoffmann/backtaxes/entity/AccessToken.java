package be.hoffmann.backtaxes.entity;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Token d'acces opaque (OAT) pour l'authentification.
 *
 * Le token est un identifiant aleatoire stocke en base de donnees.
 * Il n'encode pas d'information (contrairement a JWT) et doit etre
 * valide contre la BDD a chaque requete.
 */
@Entity
@Table(name = "access_tokens")
public class AccessToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Valeur du token (UUID ou chaine aleatoire securisee).
     * Transmis dans le header Authorization: Bearer <token>
     */
    @Column(nullable = false, unique = true, length = 64)
    private String token;

    /** Utilisateur proprietaire du token */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Identifiant du client qui a cree le token.
     * Ex: "nuxt-bff", "mobile-app", "third-party-xyz"
     */
    @Column(name = "client_name", length = 100)
    private String clientName;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    /** Date d'expiration du token */
    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    /** Derniere utilisation du token (mise a jour a chaque validation) */
    @Column(name = "last_used_at")
    private Instant lastUsedAt;

    // ==================== CONSTRUCTEURS ====================

    public AccessToken() {
    }

    public AccessToken(String token, User user, String clientName, Instant expiresAt) {
        this.token = token;
        this.user = user;
        this.clientName = clientName;
        this.expiresAt = expiresAt;
    }

    // ==================== METHODES UTILITAIRES ====================

    /**
     * Verifie si le token est expire.
     * @return true si le token a depasse sa date d'expiration
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getClientName() {
        return clientName;
    }

    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Instant getLastUsedAt() {
        return lastUsedAt;
    }

    public void setLastUsedAt(Instant lastUsedAt) {
        this.lastUsedAt = lastUsedAt;
    }
}
