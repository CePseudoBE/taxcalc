package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.AccessToken;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.repository.AccessTokenRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

/**
 * Service de gestion des tokens d'acces opaques (OAT).
 *
 * Responsabilites:
 * - Generation de tokens securises
 * - Validation des tokens
 * - Revocation (logout)
 * - Nettoyage des tokens expires
 */
@Service
@Transactional(readOnly = true)
public class TokenService {

    private static final Logger log = LoggerFactory.getLogger(TokenService.class);
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final Base64.Encoder ENCODER = Base64.getUrlEncoder().withoutPadding();

    private final AccessTokenRepository tokenRepository;

    @Value("${app.token.expiration-hours:24}")
    private int tokenExpirationHours;

    @Value("${app.token.length:32}")
    private int tokenLength;

    public TokenService(AccessTokenRepository tokenRepository) {
        this.tokenRepository = tokenRepository;
    }

    /**
     * Genere un nouveau token d'acces pour un utilisateur.
     *
     * @param user L'utilisateur pour lequel generer le token
     * @param clientName Identifiant du client (ex: "nuxt-bff", "mobile-app")
     * @return Le token genere et persiste
     */
    @Transactional
    public AccessToken generateToken(User user, String clientName) {
        String tokenValue = generateSecureToken();
        Instant expiresAt = Instant.now().plus(Duration.ofHours(tokenExpirationHours));

        AccessToken token = new AccessToken(tokenValue, user, clientName, expiresAt);
        return tokenRepository.save(token);
    }

    /**
     * Valide un token et retourne l'utilisateur associe si valide.
     * Met a jour la date de derniere utilisation.
     *
     * @param tokenValue La valeur du token a valider
     * @return L'utilisateur si le token est valide et non expire
     */
    @Transactional
    public Optional<User> validateToken(String tokenValue) {
        return tokenRepository.findByTokenWithUser(tokenValue)
                .filter(token -> !token.isExpired())
                .map(token -> {
                    // Mettre a jour la date de derniere utilisation
                    token.setLastUsedAt(Instant.now());
                    tokenRepository.save(token);
                    return token.getUser();
                });
    }

    /**
     * Revoque un token specifique (logout).
     *
     * @param tokenValue La valeur du token a revoquer
     */
    @Transactional
    public void revokeToken(String tokenValue) {
        tokenRepository.deleteByToken(tokenValue);
    }

    /**
     * Revoque tous les tokens d'un utilisateur (logout de tous les appareils).
     *
     * @param userId L'ID de l'utilisateur
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        tokenRepository.deleteAllByUserId(userId);
    }

    /**
     * Retourne la duree de vie des tokens en secondes.
     * Utile pour informer le client de la duree avant expiration.
     */
    public long getTokenExpirationSeconds() {
        return Duration.ofHours(tokenExpirationHours).toSeconds();
    }

    /**
     * Nettoyage automatique des tokens expires.
     * Execute toutes les heures.
     */
    @Scheduled(fixedRate = 3600000) // 1 heure en millisecondes
    @Transactional
    public void cleanupExpiredTokens() {
        int deleted = tokenRepository.deleteExpiredTokens(Instant.now());
        if (deleted > 0) {
            log.info("Cleaned up {} expired access tokens", deleted);
        }
    }

    /**
     * Genere un token cryptographiquement securise.
     * Utilise SecureRandom et encodage Base64 URL-safe.
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[tokenLength];
        SECURE_RANDOM.nextBytes(randomBytes);
        return ENCODER.encodeToString(randomBytes);
    }
}
