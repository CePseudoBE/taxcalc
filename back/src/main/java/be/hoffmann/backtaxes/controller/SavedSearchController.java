package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.request.SavedSearchRequest;
import be.hoffmann.backtaxes.dto.response.ApiResponse;
import be.hoffmann.backtaxes.dto.response.SavedSearchResponse;
import be.hoffmann.backtaxes.entity.SavedSearch;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.service.SavedSearchService;
import be.hoffmann.backtaxes.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Recherches sauvegardees", description = "Gestion des recherches sauvegardees par l'utilisateur")
@RestController
@RequestMapping("/api/saved-searches")
public class SavedSearchController {

    private static final String USER_SESSION_KEY = "user_id";

    private final SavedSearchService savedSearchService;
    private final UserService userService;

    public SavedSearchController(SavedSearchService savedSearchService, UserService userService) {
        this.savedSearchService = savedSearchService;
        this.userService = userService;
    }

    @Operation(summary = "Sauvegarder une recherche", description = "Sauvegarde une recherche de vehicule pour l'utilisateur connecte")
    @PostMapping
    public ResponseEntity<ApiResponse<SavedSearchResponse>> saveSearch(
            @Valid @RequestBody SavedSearchRequest request,
            HttpSession session) {

        User user = getCurrentUser(session);
        SavedSearch savedSearch = savedSearchService.save(request, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(savedSearchService.toResponse(savedSearch), "Search saved."));
    }

    @Operation(summary = "Mes recherches", description = "Liste les recherches sauvegardees de l'utilisateur connecte")
    @GetMapping
    public ResponseEntity<ApiResponse<List<SavedSearchResponse>>> getMySavedSearches(HttpSession session) {
        User user = getCurrentUser(session);
        List<SavedSearch> savedSearches = savedSearchService.findByUser(user.getId());
        return ResponseEntity.ok(ApiResponse.success(savedSearchService.toResponseList(savedSearches)));
    }

    @Operation(summary = "Detail d'une recherche", description = "Recupere une recherche sauvegardee par son ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SavedSearchResponse>> getSavedSearch(
            @Parameter(description = "ID de la recherche") @PathVariable Long id,
            HttpSession session) {

        User user = getCurrentUser(session);
        SavedSearch savedSearch = savedSearchService.findById(id);

        // Verifier que l'utilisateur est le proprietaire
        if (!savedSearch.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied.", "FORBIDDEN"));
        }

        return ResponseEntity.ok(ApiResponse.success(savedSearchService.toResponse(savedSearch)));
    }

    @Operation(summary = "Supprimer une recherche", description = "Supprime une recherche sauvegardee")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSavedSearch(
            @Parameter(description = "ID de la recherche") @PathVariable Long id,
            HttpSession session) {

        User user = getCurrentUser(session);
        savedSearchService.delete(id, user.getId());
        return ResponseEntity.ok(ApiResponse.success("Search deleted."));
    }

    /**
     * Recupere l'utilisateur connecte depuis la session.
     * En mode dev local (sans auth), utilise l'utilisateur ID 1 par defaut.
     */
    private User getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute(USER_SESSION_KEY);
        if (userId == null) {
            // Mode dev: tenter d'utiliser l'utilisateur par defaut (ID 1)
            try {
                return userService.findById(1L);
            } catch (Exception e) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not authenticated");
            }
        }
        return userService.findById(userId);
    }
}
