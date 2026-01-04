package be.hoffmann.backtaxes.entity;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Type de reduction applicable aux utilisateurs.
 *
 * Certains contribuables peuvent beneficier de reductions:
 *   - large_family: Famille nombreuse (3+ enfants a charge)
 *   - single_parent: Famille monoparentale
 *
 * Cette table est une "lookup table" - elle contient les types possibles.
 * Les montants reels sont dans UserReduction (varies par region).
 */
@Entity
@Table(name = "user_reduction_types")
public class UserReductionType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Code unique du type de reduction */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /** Description lisible du type */
    @Column(columnDefinition = "text")
    private String description;

    /** Les reductions concretes associees a ce type */
    @OneToMany(mappedBy = "reductionType", cascade = CascadeType.ALL)
    private List<UserReduction> reductions = new ArrayList<>();

    // ==================== CONSTRUCTEURS ====================

    public UserReductionType() {
    }

    public UserReductionType(String code, String description) {
        this.code = code;
        this.description = description;
    }

    // ==================== GETTERS & SETTERS ====================

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<UserReduction> getReductions() {
        return reductions;
    }

    public void setReductions(List<UserReduction> reductions) {
        this.reductions = reductions;
    }
}
