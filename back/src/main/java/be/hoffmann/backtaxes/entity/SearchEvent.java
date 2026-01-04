package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.DeviceType;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import be.hoffmann.backtaxes.entity.enums.Region;
import be.hoffmann.backtaxes.entity.enums.SearchType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Evenement de recherche effectue par un utilisateur (anonyme ou connecte).
 *
 * Cette table est au coeur du systeme analytics. Elle enregistre chaque
 * interaction de recherche pour permettre:
 *   - Analyse des tendances de recherche
 *   - Identification des vehicules populaires
 *   - Comportement utilisateur (device, source, langue)
 *
 * RGPD: Pas d'IP stockee. Session anonymisee via UUID.
 * Retention: Donnees brutes purgees apres 90 jours, agregats conserves.
 */
@Entity
@Table(name = "search_events")
public class SearchEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identifiant de session anonyme (cookie).
     * Permet de grouper les recherches d'un meme visiteur sans l'identifier.
     */
    @Column(name = "session_id", nullable = false)
    private UUID sessionId;

    /**
     * Utilisateur connecte (optionnel).
     * Null pour les visiteurs anonymes.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    /** Marque recherchee (optionnel) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

    /** Modele recherche (optionnel) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id")
    private Model model;

    /** Variante specifique (optionnel, si recherche precise) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private Variant variant;

    /** Region de calcul selectionnee */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(columnDefinition = "region")
    private Region region;

    /** Type de carburant filtre */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "fuel_type", columnDefinition = "fuel_type")
    private FuelType fuelType;

    /** Vehicule neuf ou occasion */
    @Column(name = "is_new_vehicle")
    private Boolean isNewVehicle;

    /** Date de premiere immatriculation (pour occasion) */
    @Column(name = "first_registration_date")
    private LocalDate firstRegistrationDate;

    /** Type de recherche effectuee */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "search_type", nullable = false, columnDefinition = "search_type")
    private SearchType searchType;

    /** Type d'appareil utilise */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "device_type", columnDefinition = "device_type")
    private DeviceType deviceType;

    /** Source du trafic (google, direct, facebook, etc.) */
    @Column(name = "referrer_source", length = 100)
    private String referrerSource;

    /**
     * Hash SHA-256 du User-Agent.
     * Anonymise pour RGPD tout en permettant l'analyse des navigateurs.
     */
    @Column(name = "user_agent_hash", length = 64)
    private String userAgentHash;

    /** Langue du navigateur (fr, nl, de, en) */
    @Column(length = 10)
    private String language;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // ==================== CONSTRUCTEURS ====================

    public SearchEvent() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getSessionId() {
        return sessionId;
    }

    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public Variant getVariant() {
        return variant;
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    public Region getRegion() {
        return region;
    }

    public void setRegion(Region region) {
        this.region = region;
    }

    public FuelType getFuelType() {
        return fuelType;
    }

    public void setFuelType(FuelType fuelType) {
        this.fuelType = fuelType;
    }

    public Boolean getIsNewVehicle() {
        return isNewVehicle;
    }

    public void setIsNewVehicle(Boolean isNewVehicle) {
        this.isNewVehicle = isNewVehicle;
    }

    public LocalDate getFirstRegistrationDate() {
        return firstRegistrationDate;
    }

    public void setFirstRegistrationDate(LocalDate firstRegistrationDate) {
        this.firstRegistrationDate = firstRegistrationDate;
    }

    public SearchType getSearchType() {
        return searchType;
    }

    public void setSearchType(SearchType searchType) {
        this.searchType = searchType;
    }

    public DeviceType getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(DeviceType deviceType) {
        this.deviceType = deviceType;
    }

    public String getReferrerSource() {
        return referrerSource;
    }

    public void setReferrerSource(String referrerSource) {
        this.referrerSource = referrerSource;
    }

    public String getUserAgentHash() {
        return userAgentHash;
    }

    public void setUserAgentHash(String userAgentHash) {
        this.userAgentHash = userAgentHash;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
