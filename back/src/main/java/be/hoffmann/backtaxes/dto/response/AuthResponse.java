package be.hoffmann.backtaxes.dto.response;

/**
 * Reponse pour les endpoints d'authentification (login/register).
 * Contient les informations utilisateur et le token d'acces.
 *
 * @param user Informations de l'utilisateur connecte
 * @param accessToken Token d'acces opaque (OAT)
 * @param expiresIn Duree de validite du token en secondes
 */
public record AuthResponse(UserResponse user, String accessToken, long expiresIn) {
}
