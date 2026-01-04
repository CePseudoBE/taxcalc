package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.PendingCalculation;
import be.hoffmann.backtaxes.entity.PendingCalculation.Status;
import be.hoffmann.backtaxes.entity.enums.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PendingCalculationRepository extends JpaRepository<PendingCalculation, Long> {

    List<PendingCalculation> findByStatus(Status status);

    List<PendingCalculation> findByStatusAndRegion(Status status, Region region);

    List<PendingCalculation> findBySessionId(UUID sessionId);

    List<PendingCalculation> findByUserId(Long userId);
}
