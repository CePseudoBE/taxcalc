package be.hoffmann.backtaxes.repository;

import be.hoffmann.backtaxes.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {

    Optional<AccessToken> findByToken(String token);

    List<AccessToken> findByUserId(Long userId);

    boolean existsByToken(String token);

    @Modifying
    @Query("DELETE FROM AccessToken a WHERE a.token = :token")
    void deleteByToken(String token);

    @Modifying
    @Query("DELETE FROM AccessToken a WHERE a.user.id = :userId")
    void deleteAllByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM AccessToken a WHERE a.expiresAt < :now")
    int deleteExpiredTokens(Instant now);
}
