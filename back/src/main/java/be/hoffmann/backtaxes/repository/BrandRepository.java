package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {

    Optional<Brand> findByName(String name);

    boolean existsByName(String name);

    /**
     * Recupere toutes les marques avec pagination.
     */
    Page<Brand> findAllBy(Pageable pageable);
}
