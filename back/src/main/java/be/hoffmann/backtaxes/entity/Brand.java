package be.hoffmann.backtaxes.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Entite representant une marque automobile (BMW, Audi, Peugeot, etc.)
 *
 * EXPLICATION DES ANNOTATIONS:
 *
 * @Entity     -> Dit a JPA: "Cette classe represente une table en DB"
 *                Sans cette annotation, JPA ignore completement la classe.
 *
 * @Table      -> Specifie le nom de la table. Optionnel si le nom de la classe
 *                correspond au nom de la table (ici "Brand" vs "brands").
 *                On utilise "brands" (pluriel) car c'est la convention SQL.
 */
@Entity
@Table(name = "brands")
public class Brand {

    /**
     * @Id             -> Marque ce champ comme la CLE PRIMAIRE de la table.
     *                    Chaque entite DOIT avoir exactement un @Id.
     *
     * @GeneratedValue -> L'ID est genere automatiquement par la DB.
     *                    IDENTITY = utilise la sequence/auto-increment de PostgreSQL.
     *                    Tu n'as pas besoin de setter l'ID, JPA le fait pour toi.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * @Column -> Configure comment ce champ est mappe a la colonne.
     *
     *   - nullable = false  : La colonne ne peut pas etre NULL (NOT NULL en SQL)
     *   - unique = true     : Valeur unique dans toute la table (UNIQUE en SQL)
     *   - length = 100      : VARCHAR(100) - limite la taille du texte
     *
     * Si tu ne mets pas @Column, JPA utilise des valeurs par defaut:
     *   - nom de colonne = nom du champ
     *   - nullable = true
     *   - unique = false
     */
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    /**
     * Pour les timestamps, on utilise Instant (moment precis dans le temps).
     *
     * insertable = false -> JPA ne va PAS inclure ce champ dans les INSERT.
     *                       La valeur par defaut de PostgreSQL (CURRENT_TIMESTAMP) s'applique.
     *
     * updatable = false  -> JPA ne va PAS modifier ce champ lors des UPDATE.
     *                       Une fois cree, le timestamp ne change plus.
     */
    @Column(name = "created_at", insertable = false, updatable = false)
    private Instant createdAt;

    /**
     * RELATION ONE-TO-MANY: Une marque a PLUSIEURS modeles.
     *
     * @OneToMany -> "Un Brand a plusieurs Models"
     *
     *   - mappedBy = "brand" : Le champ "brand" dans Model gere cette relation.
     *                          C'est le cote "inverse" de la relation.
     *                          Model.brand est le cote "proprietaire" (avec la FK).
     *
     *   - cascade = ALL      : Les operations sur Brand se propagent aux Models.
     *                          Si tu supprimes une Brand, ses Models sont supprimes aussi.
     *
     *   - orphanRemoval      : Si tu retires un Model de la liste, il est supprime de la DB.
     *
     * IMPORTANT: On initialise avec new ArrayList<>() pour eviter les NullPointerException.
     */
    @OneToMany(mappedBy = "brand", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Model> models = new ArrayList<>();

    // ==================== CONSTRUCTEURS ====================

    /**
     * Constructeur sans argument - OBLIGATOIRE pour JPA.
     * JPA a besoin de creer des instances vides puis de remplir les champs.
     */
    public Brand() {
    }

    /**
     * Constructeur pratique pour creer une marque avec son nom.
     */
    public Brand(String name) {
        this.name = name;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public List<Model> getModels() {
        return models;
    }

    public void setModels(List<Model> models) {
        this.models = models;
    }

    // ==================== METHODES UTILITAIRES ====================

    /**
     * Methode helper pour ajouter un modele.
     * Elle maintient la coherence bidirectionnelle de la relation.
     */
    public void addModel(Model model) {
        models.add(model);
        model.setBrand(this);
    }

    public void removeModel(Model model) {
        models.remove(model);
        model.setBrand(null);
    }
}
