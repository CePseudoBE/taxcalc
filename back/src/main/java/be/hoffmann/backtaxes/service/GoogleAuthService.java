package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.repository.UserRepository;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

/**
 * Service pour l'authentification via Google OAuth.
 * Valide les ID tokens Google et cree/recupere les utilisateurs.
 */
@Service
public class GoogleAuthService {

    private static final Logger log = LoggerFactory.getLogger(GoogleAuthService.class);

    private final UserRepository userRepository;
    private final GoogleIdTokenVerifier verifier;

    public GoogleAuthService(
            UserRepository userRepository,
            @Value("${google.client-id:}") String clientId) {
        this.userRepository = userRepository;

        if (clientId == null || clientId.isBlank()) {
            log.warn("Google Client ID not configured. Google authentication will be disabled.");
            this.verifier = null;
        } else {
            this.verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(),
                    GsonFactory.getDefaultInstance())
                    .setAudience(Collections.singletonList(clientId))
                    .build();
        }
    }

    /**
     * Verifie le token Google et retourne l'utilisateur correspondant.
     * Cree un nouvel utilisateur si necessaire.
     *
     * @param idTokenString Le ID token obtenu du frontend
     * @return L'utilisateur authentifie
     */
    @Transactional
    public User authenticateWithGoogle(String idTokenString) {
        if (verifier == null) {
            throw new ValidationException("Google authentication is not configured");
        }

        try {
            GoogleIdToken idToken = verifier.verify(idTokenString);

            if (idToken == null) {
                throw new ValidationException("Invalid Google ID token");
            }

            GoogleIdToken.Payload payload = idToken.getPayload();
            String googleId = payload.getSubject();
            String email = payload.getEmail();

            if (email == null || email.isBlank()) {
                throw new ValidationException("Google account does not have an email");
            }

            // Chercher d'abord par Google ID
            return userRepository.findByGoogleId(googleId)
                    .orElseGet(() -> {
                        // Chercher si l'email existe deja (migration d'un compte existant)
                        return userRepository.findByEmail(email)
                                .map(existingUser -> {
                                    // Lier le compte Google au compte existant
                                    existingUser.setGoogleId(googleId);
                                    return userRepository.save(existingUser);
                                })
                                .orElseGet(() -> {
                                    // Creer un nouveau compte
                                    User newUser = new User();
                                    newUser.setEmail(email);
                                    newUser.setGoogleId(googleId);
                                    newUser.setIsModerator(false);
                                    newUser.setIsAdmin(false);
                                    return userRepository.save(newUser);
                                });
                    });

        } catch (ValidationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error verifying Google ID token", e);
            throw new ValidationException("Failed to verify Google token");
        }
    }

    /**
     * Verifie si Google OAuth est configure.
     */
    public boolean isEnabled() {
        return verifier != null;
    }
}
