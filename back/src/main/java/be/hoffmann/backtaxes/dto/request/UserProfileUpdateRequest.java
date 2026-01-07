package be.hoffmann.backtaxes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Requete pour la mise a jour du profil utilisateur.
 * Tous les champs sont optionnels - seuls les champs non-null seront mis a jour.
 */
public record UserProfileUpdateRequest(
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        String email,

        @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
        String newPassword,

        @Size(min = 8, max = 100, message = "Current password must be between 8 and 100 characters")
        String currentPassword
) {
}
