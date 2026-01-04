package be.hoffmann.backtaxes.dto.response;

/**
 * Reponse pour un modele de vehicule.
 */
public record ModelResponse(Long id, String name, BrandResponse brand) {
}
