package be.hoffmann.backtaxes.controller.admin;

import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller d'administration pour la gestion des utilisateurs.
 * Accessible uniquement aux administrateurs (ROLE_ADMIN).
 */
@Tag(name = "Administration - Utilisateurs", description = "Gestion des utilisateurs (acces administrateur)")
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private static final Logger audit = LoggerFactory.getLogger("audit");

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Liste des utilisateurs", description = "Liste tous les utilisateurs enregistres")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserResponse> responses = users.stream()
                .map(userService::toResponse)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @Operation(summary = "Detail d'un utilisateur", description = "Recupere un utilisateur par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUser(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(userService.toResponse(user)));
    }

    @Operation(summary = "Modifier le role d'un utilisateur", description = "Met a jour les roles (moderateur/admin) d'un utilisateur")
    @PutMapping("/{id}/role")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserRole(
            @Parameter(description = "ID de l'utilisateur") @PathVariable Long id,
            @Valid @RequestBody RoleUpdateRequest request) {

        User user = userService.findById(id);

        if (request.isModerator() != null) {
            user = userService.setModeratorStatus(id, request.isModerator());
        }

        if (request.isAdmin() != null) {
            user = userService.setAdminStatus(id, request.isAdmin());
        }

        // Audit log
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User admin) {
            audit.info("ROLE_CHANGED admin_id={} target_user_id={} target_email={} is_moderator={} is_admin={}",
                    admin.getId(), user.getId(), user.getEmail(), user.getIsModerator(), user.getIsAdmin());
        }

        return ResponseEntity.ok(ApiResponse.success(userService.toResponse(user), "User roles updated."));
    }

    /**
     * Requete pour la mise a jour des roles d'un utilisateur.
     */
    public record RoleUpdateRequest(
            @Parameter(description = "Statut moderateur")
            Boolean isModerator,

            @Parameter(description = "Statut administrateur")
            Boolean isAdmin
    ) {
    }
}
