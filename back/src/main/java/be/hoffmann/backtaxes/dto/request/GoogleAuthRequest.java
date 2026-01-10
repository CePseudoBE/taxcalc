package be.hoffmann.backtaxes.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Requete pour l'authentification via Google OAuth.
 * Le frontend envoie le ID token obtenu de Google.
 */
public record GoogleAuthRequest(
        @NotBlank(message = "Google ID token is required")
        String idToken
) {
}
