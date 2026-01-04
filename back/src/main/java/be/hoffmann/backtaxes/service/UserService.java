package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.request.UserRegistrationRequest;
import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service pour la gestion des utilisateurs.
 */
@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Enregistre un nouvel utilisateur.
     */
    @Transactional
    public User register(UserRegistrationRequest request) {
        // Verifier si l'email existe deja
        if (userRepository.existsByEmail(request.email())) {
            throw new ValidationException("email", "Email already exists");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setIsModerator(false);

        return userRepository.save(user);
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
     * Verifie les credentials d'un utilisateur.
     */
    public Optional<User> authenticate(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> passwordEncoder.matches(password, user.getPasswordHash()));
    }

    /**
     * Convertit un User en UserResponse.
     */
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getIsModerator(),
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
}
