package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs.
 * L'authentification se fait uniquement via Google OAuth (voir GoogleAuthService).
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Trouve un utilisateur par email.
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * Trouve un utilisateur par ID.
     */
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

    /**
     * Convertit un User en UserResponse.
     */
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getIsModerator(),
                user.getIsAdmin(),
                user.getCreatedAt()
        );
    }

    /**
     * Met a jour le statut moderateur d'un utilisateur.
     */
    @Transactional
    public User setModeratorStatus(Long userId, boolean isModerator) {
        User user = findById(userId);
        user.setIsModerator(isModerator);
        return userRepository.save(user);
    }

    /**
     * Supprime le compte de l'utilisateur.
     */
    @Transactional
    public void deleteAccount(Long userId) {
        User user = findById(userId);
        userRepository.delete(user);
    }

    /**
     * Liste tous les utilisateurs (pour admin).
     */
    public List<User> findAll() {
        return userRepository.findAll();
    }

    /**
     * Met a jour le statut admin d'un utilisateur.
     */
    @Transactional
    public User setAdminStatus(Long userId, boolean isAdmin) {
        User user = findById(userId);
        user.setIsAdmin(isAdmin);
        return userRepository.save(user);
    }
}
