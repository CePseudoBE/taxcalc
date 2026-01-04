package be.hoffmann.backtaxes.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entite representant un modele de vehicule (Serie 3, A4, 308, etc.)
 * Un modele appartient a une marque et contient plusieurs variantes.
 */
@Entity
@Table(
    name = "models",
    // Contrainte d'unicite composite: la combinaison (brand_id, name) doit etre unique.
    // Tu ne peux pas avoir deux "Serie 3" pour BMW, mais tu peux avoir
    // "Serie 3" chez BMW et "Serie 3" chez... personne d'autre en fait :)
    uniqueConstraints = @UniqueConstraint(columnNames = {"brand_id", "name"})
)
public class Model {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * RELATION MANY-TO-ONE: Plusieurs modeles appartiennent a UNE marque.
     *
     * @ManyToOne -> "Plusieurs Models pour un Brand"
     *
     * @JoinColumn -> Specifie la colonne de cle etrangere (FK).
     *   - name = "brand_id"  : Nom de la colonne FK dans la table 'models'
     *   - nullable = false   : Chaque modele DOIT avoir une marque
     *
     * C'est le cote "proprietaire" de la relation - c'est lui qui a la FK.
     *
     * fetch = LAZY vs EAGER:
     *   - LAZY (par defaut pour @ManyToOne? Non, c'est EAGER!)
     *           -> Charge la Brand seulement quand tu y accedes
     *   - EAGER -> Charge la Brand immediatement avec le Model
     *
     * On met LAZY pour eviter de charger trop de donnees inutilement.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    /**
     * Un modele a plusieurs variantes (ex: "320d 150ch", "330i 258ch", etc.)
     */
    @OneToMany(mappedBy = "model", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Variant> variants = new ArrayList<>();

    // ==================== CONSTRUCTEURS ====================

    public Model() {
    }

    public Model(Brand brand, String name) {
        this.brand = brand;
        this.name = name;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public List<Variant> getVariants() {
        return variants;
    }

    public void setVariants(List<Variant> variants) {
        this.variants = variants;
    }

    // ==================== METHODES UTILITAIRES ====================

    public void addVariant(Variant variant) {
        variants.add(variant);
        variant.setModel(this);
    }

    public void removeVariant(Variant variant) {
        variants.remove(variant);
        variant.setModel(null);
    }
}
