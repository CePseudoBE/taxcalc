package be.hoffmann.backtaxes.entity;

import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

/**
 * Variante/version d'un modele avec toutes les specifications techniques.
 * Exemple: BMW Serie 3 -> "320d 150ch BVA8 M Sport"
 *
 * C'est l'entite la plus detaillee car elle contient toutes les infos
 * necessaires au calcul des taxes.
 */
@Entity
@Table(
    name = "variants",
    uniqueConstraints = @UniqueConstraint(columnNames = {"model_id", "name", "year_start"})
)
public class Variant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "model_id", nullable = false)
    private Model model;

    /** Nom de la variante, ex: "2.0 TDI 150ch S-Line" */
    @Column(nullable = false, length = 150)
    private String name;

    /** Annee de debut de production */
    @Column(name = "year_start", nullable = false)
    private Integer yearStart;

    /** Annee de fin de production (null = toujours en production) */
    @Column(name = "year_end")
    private Integer yearEnd;

    /** Cylindree en cm3 (ex: 1968 pour un 2.0L) */
    @Column(name = "displacement_cc")
    private Integer displacementCc;

    /** Puissance en kW (ex: 110 kW = ~150 ch) */
    @Column(name = "power_kw", nullable = false)
    private Integer powerKw;

    /**
     * Chevaux fiscaux (CV) - calcules a partir de la cylindree.
     * Formule belge: CV = cylindree / 200 (arrondi)
     * Utilises pour la taxe annuelle.
     */
    @Column(name = "fiscal_hp", nullable = false)
    private Integer fiscalHp;

    /**
     * Type de carburant/energie.
     *
     * @Enumerated(EnumType.STRING) -> Stocke le NOM de l'enum en DB ("diesel")
     *                                 au lieu de l'ORDINAL (1, 2, 3...).
     *
     * TOUJOURS utiliser STRING! Pourquoi?
     *   - ORDINAL: Si tu ajoutes un enum au milieu, tous les numeros changent!
     *   - STRING: Le nom reste stable, meme si tu reorganises l'enum.
     *
     * @JdbcTypeCode(SqlTypes.NAMED_ENUM) -> Dit a Hibernate 6+ d'utiliser le type
     *                                       enum natif de PostgreSQL pour les requetes.
     */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "fuel_type")
    private FuelType fuel;

    /** Norme Euro d'emissions */
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "euro_norm", nullable = false, columnDefinition = "euro_norm")
    private EuroNorm euroNorm;

    /**
     * Emissions CO2 selon le cycle WLTP (nouveau, depuis 2017).
     * Plus realiste que NEDC.
     */
    @Column(name = "co2_wltp")
    private Integer co2Wltp;

    /** Emissions CO2 selon le cycle NEDC (ancien, avant 2017) */
    @Column(name = "co2_nedc")
    private Integer co2Nedc;

    /** Masse Maximale Autorisee en kg (poids du vehicule + charge max) */
    @Column(name = "mma_kg")
    private Integer mmaKg;

    /** Presence d'un filtre a particules (important pour certains calculs) */
    @Column(name = "has_particle_filter")
    private Boolean hasParticleFilter;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    // ==================== CONSTRUCTEURS ====================

    public Variant() {
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Model getModel() {
        return model;
    }

    public void setModel(Model model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getYearStart() {
        return yearStart;
    }

    public void setYearStart(Integer yearStart) {
        this.yearStart = yearStart;
    }

    public Integer getYearEnd() {
        return yearEnd;
    }

    public void setYearEnd(Integer yearEnd) {
        this.yearEnd = yearEnd;
    }

    public Integer getDisplacementCc() {
        return displacementCc;
    }

    public void setDisplacementCc(Integer displacementCc) {
        this.displacementCc = displacementCc;
    }

    public Integer getPowerKw() {
        return powerKw;
    }

    public void setPowerKw(Integer powerKw) {
        this.powerKw = powerKw;
    }

    public Integer getFiscalHp() {
        return fiscalHp;
    }

    public void setFiscalHp(Integer fiscalHp) {
        this.fiscalHp = fiscalHp;
    }

    public FuelType getFuel() {
        return fuel;
    }

    public void setFuel(FuelType fuel) {
        this.fuel = fuel;
    }

    public EuroNorm getEuroNorm() {
        return euroNorm;
    }

    public void setEuroNorm(EuroNorm euroNorm) {
        this.euroNorm = euroNorm;
    }

    public Integer getCo2Wltp() {
        return co2Wltp;
    }

    public void setCo2Wltp(Integer co2Wltp) {
        this.co2Wltp = co2Wltp;
    }

    public Integer getCo2Nedc() {
        return co2Nedc;
    }

    public void setCo2Nedc(Integer co2Nedc) {
        this.co2Nedc = co2Nedc;
    }

    public Integer getMmaKg() {
        return mmaKg;
    }

    public void setMmaKg(Integer mmaKg) {
        this.mmaKg = mmaKg;
    }

    public Boolean getHasParticleFilter() {
        return hasParticleFilter;
    }

    public void setHasParticleFilter(Boolean hasParticleFilter) {
        this.hasParticleFilter = hasParticleFilter;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
