package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.LoginRequest;
import be.hoffmann.backtaxes.dto.request.UserRegistrationRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentification", description = "Inscription, connexion et gestion du profil utilisateur")
@RestController
@RequestMapping("/api")
public class UserController {

    private static final String USER_SESSION_KEY = "user_id";

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Inscription", description = "Enregistre un nouvel utilisateur et le connecte automatiquement")
    @PostMapping("/auth/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(
            @Valid @RequestBody UserRegistrationRequest request,
            HttpSession session) {

        User user = userService.register(request);

        // Connecter automatiquement apres inscription
        session.setAttribute(USER_SESSION_KEY, user.getId());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(userService.toResponse(user), "Registration successful."));
    }

    @Operation(summary = "Connexion", description = "Authentifie un utilisateur avec email et mot de passe")
    @PostMapping("/auth/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {

        User user = userService.authenticate(request.email(), request.password())
                .orElseThrow(() -> new ValidationException("Invalid email or password"));

        session.setAttribute(USER_SESSION_KEY, user.getId());

        return ResponseEntity.ok(ApiResponse.success(userService.toResponse(user), "Login successful."));
    }

    @Operation(summary = "Deconnexion", description = "Deconnecte l'utilisateur et invalide sa session")
    @PostMapping("/auth/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok(ApiResponse.success("Logout successful."));
    }

    @Operation(summary = "Profil utilisateur", description = "Retourne le profil de l'utilisateur connecte")
    @GetMapping("/users/me")
    public ResponseEntity<ApiResponse<UserResponse>> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_SESSION_KEY);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated.", "UNAUTHORIZED"));
        }

        User user = userService.findById(userId);
        return ResponseEntity.ok(ApiResponse.success(userService.toResponse(user)));
    }

    @Operation(summary = "Verification session", description = "Verifie si l'utilisateur est connecte et retourne son profil")
    @GetMapping("/auth/check")
    public ResponseEntity<ApiResponse<UserResponse>> checkAuth(HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_SESSION_KEY);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Not authenticated.", "UNAUTHORIZED"));
        }

        try {
            User user = userService.findById(userId);
            return ResponseEntity.ok(ApiResponse.success(userService.toResponse(user)));
        } catch (Exception e) {
            session.invalidate();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid session.", "UNAUTHORIZED"));
        }
    }
}
