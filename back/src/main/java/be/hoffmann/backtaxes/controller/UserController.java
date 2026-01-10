package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.GoogleAuthRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.AuthResponse;
import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.AccessToken;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.service.GoogleAuthService;
import be.hoffmann.backtaxes.service.TokenService;
import be.hoffmann.backtaxes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller pour l'authentification et la gestion du profil utilisateur.
 *
 * Utilise des tokens opaques (OAT) au lieu des sessions HTTP.
 */
@Tag(name = "Authentification", description = "Inscription, connexion et gestion du profil utilisateur")
@RestController
@RequestMapping("/api")
public class UserController {

    private static final Logger audit = LoggerFactory.getLogger("audit");
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;
    private final TokenService tokenService;
    private final GoogleAuthService googleAuthService;

    public UserController(UserService userService, TokenService tokenService, GoogleAuthService googleAuthService) {
        this.userService = userService;
        this.tokenService = tokenService;
        this.googleAuthService = googleAuthService;
    }

    @Operation(summary = "Connexion Google", description = "Authentifie un utilisateur via Google OAuth et retourne un token d'acces")
    @PostMapping("/auth/google")
    public ResponseEntity<ApiResponse<AuthResponse>> googleAuth(
            @Valid @RequestBody GoogleAuthRequest request,
            @RequestHeader(value = "X-Client-Name", defaultValue = "unknown") String clientName) {

        User user = googleAuthService.authenticateWithGoogle(request.idToken());
        AccessToken token = tokenService.generateToken(user, clientName);

        audit.info("LOGIN user_id={} email={} client={}", user.getId(), user.getEmail(), clientName);

        AuthResponse authResponse = new AuthResponse(
                userService.toResponse(user),
                token.getToken(),
                tokenService.getTokenExpirationSeconds()
        );

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Google login successful."));
    }

    @Operation(summary = "Deconnexion", description = "Revoque le token d'acces actuel")
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            tokenService.revokeToken(token);
        }
        return ResponseEntity.ok(ApiResponse.success("Logout successful."));
    }

    @Operation(summary = "Deconnexion globale", description = "Revoque tous les tokens de l'utilisateur (deconnexion de tous les appareils)")
    @PostMapping("/auth/logout-all")
    public ResponseEntity<ApiResponse<Void>> logoutAll() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            tokenService.revokeAllUserTokens(user.getId());
            audit.info("LOGOUT_ALL user_id={} email={}", user.getId(), user.getEmail());
        }
        return ResponseEntity.ok(ApiResponse.success("All sessions terminated."));
    }

    @Operation(summary = "Profil utilisateur", description = "Retourne le profil de l'utilisateur connecte")
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated.", "UNAUTHORIZED"));
        }

        return ResponseEntity.ok(ApiResponse.success(userService.toResponse(user)));
    }

    @Operation(summary = "Verification session", description = "Verifie si le token est valide et retourne le profil utilisateur")
    @GetMapping("/auth/check")
    public ResponseEntity<ApiResponse<UserResponse>> checkAuth() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated.", "UNAUTHORIZED"));
        }

        return ResponseEntity.ok(ApiResponse.success(userService.toResponse(user)));
    }

    @Operation(summary = "Suppression du compte", description = "Supprime le compte de l'utilisateur connecte et revoque tous ses tokens")
    @DeleteMapping("/users/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated.", "UNAUTHORIZED"));
        }

        // Revoquer tous les tokens de l'utilisateur avant suppression
        tokenService.revokeAllUserTokens(user.getId());
        // Supprimer le compte
        userService.deleteAccount(user.getId());

        audit.info("ACCOUNT_DELETED user_id={} email={}", user.getId(), user.getEmail());

        return ResponseEntity.ok(ApiResponse.success("Account deleted."));
    }
}
