package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {

    /**
     * Charge un model avec sa brand en une seule requete.
     * Evite le probleme N+1.
     */
    @Query("SELECT m FROM Model m JOIN FETCH m.brand WHERE m.id = :id")
    Optional<Model> findByIdWithBrand(@Param("id") Long id);

    /**
     * Charge tous les models d'une brand avec la brand pre-chargee.
     */
    @Query("SELECT m FROM Model m JOIN FETCH m.brand WHERE m.brand.id = :brandId")
    List<Model> findByBrandIdWithBrand(@Param("brandId") Long brandId);

    List<Model> findByBrandId(Long brandId);

    Optional<Model> findByBrandIdAndName(Long brandId, String name);

    boolean existsByBrandIdAndName(Long brandId, String name);

    /**
     * Recherche par nom avec JOIN FETCH pour eviter N+1.
     */
    @Query("SELECT m FROM Model m JOIN FETCH m.brand WHERE LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Model> searchByNameWithBrand(@Param("name") String name);

    /**
     * Recherche par brand et nom avec JOIN FETCH.
     */
    @Query("SELECT m FROM Model m JOIN FETCH m.brand WHERE m.brand.id = :brandId AND LOWER(m.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Model> searchByBrandIdAndNameWithBrand(@Param("brandId") Long brandId, @Param("name") String name);

    List<Model> findByNameContainingIgnoreCase(String name);

    List<Model> findByBrandIdAndNameContainingIgnoreCase(Long brandId, String name);
}
