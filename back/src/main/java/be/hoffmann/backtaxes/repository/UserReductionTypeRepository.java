package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.UserReductionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserReductionTypeRepository extends JpaRepository<UserReductionType, Long> {

    Optional<UserReductionType> findByCode(String code);

    boolean existsByCode(String code);
}
