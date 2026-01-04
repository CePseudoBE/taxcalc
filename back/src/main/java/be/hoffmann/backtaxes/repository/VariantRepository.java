package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.Variant;
import be.hoffmann.backtaxes.entity.enums.EuroNorm;
import be.hoffmann.backtaxes.entity.enums.FuelType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VariantRepository extends JpaRepository<Variant, Long> {

    /**
     * Charge un variant avec son model et sa brand en une seule requete.
     * Evite le probleme N+1.
     */
    @Query("SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand " +
           "WHERE v.id = :id")
    Optional<Variant> findByIdWithModelAndBrand(@Param("id") Long id);

    List<Variant> findByModelId(Long modelId);

    /**
     * Charge les variantes d'un modele avec JOIN FETCH pour eviter N+1.
     */
    @Query("SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand " +
           "WHERE m.id = :modelId")
    List<Variant> findByModelIdWithDetails(@Param("modelId") Long modelId);

    Optional<Variant> findByModelIdAndNameAndYearStart(Long modelId, String name, Integer yearStart);

    List<Variant> findByFuel(FuelType fuel);

    /**
     * Charge les variantes par type de carburant avec JOIN FETCH.
     */
    @Query("SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand " +
           "WHERE v.fuel = :fuel")
    List<Variant> findByFuelWithDetails(@Param("fuel") FuelType fuel);

    /**
     * Charge les variantes d'une marque avec JOIN FETCH.
     */
    @Query("SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand b " +
           "WHERE b.id = :brandId")
    List<Variant> findByBrandIdWithDetails(@Param("brandId") Long brandId);

    @Query("SELECT v FROM Variant v JOIN v.model m JOIN m.brand b WHERE b.id = :brandId")
    List<Variant> findByBrandId(@Param("brandId") Long brandId);

    /**
     * Recherche de variantes par mot-cle avec JOIN FETCH pour eviter N+1.
     */
    @Query("SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand b " +
           "WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Variant> searchByKeyword(@Param("search") String search);

    /**
     * Recherche paginee de variantes par mot-cle.
     * Note: JOIN FETCH avec pagination declenche une requete count supplementaire.
     */
    @Query(value = "SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand b " +
           "WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))",
           countQuery = "SELECT COUNT(v) FROM Variant v " +
           "JOIN v.model m JOIN m.brand b " +
           "WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(m.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
           "OR LOWER(v.name) LIKE LOWER(CONCAT('%', :search, '%'))")
    Page<Variant> searchByKeywordPaginated(@Param("search") String search, Pageable pageable);

    List<Variant> findByEuroNorm(EuroNorm euroNorm);

    /**
     * Charge les variantes par norme Euro avec JOIN FETCH.
     */
    @Query("SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand " +
           "WHERE v.euroNorm = :euroNorm")
    List<Variant> findByEuroNormWithDetails(@Param("euroNorm") EuroNorm euroNorm);

    /**
     * Recherche avancee de variantes avec JOIN FETCH pour eviter N+1.
     */
    @Query("SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand b " +
           "WHERE (:brandId IS NULL OR b.id = :brandId) " +
           "AND (:modelId IS NULL OR m.id = :modelId) " +
           "AND (:fuelTypes IS NULL OR v.fuel IN :fuelTypes) " +
           "AND (:euroNorms IS NULL OR v.euroNorm IN :euroNorms) " +
           "AND (:minPower IS NULL OR v.powerKw >= :minPower) " +
           "AND (:maxPower IS NULL OR v.powerKw <= :maxPower) " +
           "AND (:minYear IS NULL OR v.yearStart >= :minYear) " +
           "AND (:maxYear IS NULL OR v.yearStart <= :maxYear) " +
           "AND (:maxCo2 IS NULL OR v.co2Wltp <= :maxCo2)")
    List<Variant> search(
            @Param("brandId") Long brandId,
            @Param("modelId") Long modelId,
            @Param("fuelTypes") List<FuelType> fuelTypes,
            @Param("euroNorms") List<EuroNorm> euroNorms,
            @Param("minPower") Integer minPower,
            @Param("maxPower") Integer maxPower,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("maxCo2") Integer maxCo2
    );

    /**
     * Recherche avancee paginee de variantes.
     */
    @Query(value = "SELECT v FROM Variant v " +
           "JOIN FETCH v.model m " +
           "JOIN FETCH m.brand b " +
           "WHERE (:brandId IS NULL OR b.id = :brandId) " +
           "AND (:modelId IS NULL OR m.id = :modelId) " +
           "AND (:fuelTypes IS NULL OR v.fuel IN :fuelTypes) " +
           "AND (:euroNorms IS NULL OR v.euroNorm IN :euroNorms) " +
           "AND (:minPower IS NULL OR v.powerKw >= :minPower) " +
           "AND (:maxPower IS NULL OR v.powerKw <= :maxPower) " +
           "AND (:minYear IS NULL OR v.yearStart >= :minYear) " +
           "AND (:maxYear IS NULL OR v.yearStart <= :maxYear) " +
           "AND (:maxCo2 IS NULL OR v.co2Wltp <= :maxCo2)",
           countQuery = "SELECT COUNT(v) FROM Variant v " +
           "JOIN v.model m JOIN m.brand b " +
           "WHERE (:brandId IS NULL OR b.id = :brandId) " +
           "AND (:modelId IS NULL OR m.id = :modelId) " +
           "AND (:fuelTypes IS NULL OR v.fuel IN :fuelTypes) " +
           "AND (:euroNorms IS NULL OR v.euroNorm IN :euroNorms) " +
           "AND (:minPower IS NULL OR v.powerKw >= :minPower) " +
           "AND (:maxPower IS NULL OR v.powerKw <= :maxPower) " +
           "AND (:minYear IS NULL OR v.yearStart >= :minYear) " +
           "AND (:maxYear IS NULL OR v.yearStart <= :maxYear) " +
           "AND (:maxCo2 IS NULL OR v.co2Wltp <= :maxCo2)")
    Page<Variant> searchPaginated(
            @Param("brandId") Long brandId,
            @Param("modelId") Long modelId,
            @Param("fuelTypes") List<FuelType> fuelTypes,
            @Param("euroNorms") List<EuroNorm> euroNorms,
            @Param("minPower") Integer minPower,
            @Param("maxPower") Integer maxPower,
            @Param("minYear") Integer minYear,
            @Param("maxYear") Integer maxYear,
            @Param("maxCo2") Integer maxCo2,
            Pageable pageable
    );
}
