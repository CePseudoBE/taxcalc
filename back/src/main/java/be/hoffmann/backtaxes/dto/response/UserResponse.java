package be.hoffmann.backtaxes.dto.response;

import java.time.Instant;

/**
 * Reponse pour un utilisateur.
 * Ne contient jamais le mot de passe.
 */
public record UserResponse(Long id, String email, Boolean isModerator, Boolean isAdmin, Instant createdAt) {
}
