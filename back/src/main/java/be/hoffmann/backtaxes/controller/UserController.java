package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.LoginRequest;
import be.hoffmann.backtaxes.dto.request.UserProfileUpdateRequest;
import be.hoffmann.backtaxes.dto.request.UserRegistrationRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.AuthResponse;
import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.AccessToken;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.service.TokenService;
import be.hoffmann.backtaxes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
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

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final UserService userService;
    private final TokenService tokenService;

    public UserController(UserService userService, TokenService tokenService) {
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @Operation(summary = "Inscription", description = "Enregistre un nouvel utilisateur et retourne un token d'acces")
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request,
            @RequestHeader(value = "X-Client-Name", defaultValue = "unknown") String clientName) {

        User user = userService.register(request);
        AccessToken token = tokenService.generateToken(user, clientName);

        AuthResponse authResponse = new AuthResponse(
                userService.toResponse(user),
                token.getToken(),
                tokenService.getTokenExpirationSeconds()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(authResponse, "Registration successful."));
    }

    @Operation(summary = "Connexion", description = "Authentifie un utilisateur et retourne un token d'acces")
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            @RequestHeader(value = "X-Client-Name", defaultValue = "unknown") String clientName) {

        User user = userService.authenticate(request.email(), request.password())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        AccessToken token = tokenService.generateToken(user, clientName);

        AuthResponse authResponse = new AuthResponse(
                userService.toResponse(user),
                token.getToken(),
                tokenService.getTokenExpirationSeconds()
        );

        return ResponseEntity.ok(ApiResponse.success(authResponse, "Login successful."));
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

    @Operation(summary = "Mise a jour du profil", description = "Met a jour le profil de l'utilisateur connecte (email et/ou mot de passe)")
    @PutMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @Valid @RequestBody UserProfileUpdateRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof User user)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated.", "UNAUTHORIZED"));
        }

        User updatedUser = userService.updateProfile(user, request);
        return ResponseEntity.ok(ApiResponse.success(userService.toResponse(updatedUser), "Profile updated."));
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

        return ResponseEntity.ok(ApiResponse.success("Account deleted."));
    }
}
