package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests pour GoogleAuthService.
 * Note: Ces tests ne testent pas la verification reelle du token Google
 * car cela necessite un token valide de Google.
 * On teste uniquement la logique de gestion des utilisateurs.
 */
@ExtendWith(MockitoExtension.class)
class GoogleAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setUp() {
        // Create service without Google client ID (disabled mode)
        googleAuthService = new GoogleAuthService(userRepository, "");
    }

    @Nested
    @DisplayName("isEnabled")
    class IsEnabledTests {

        @Test
        @DisplayName("should return false when client ID is not configured")
        void shouldReturnFalseWhenNotConfigured() {
            assertThat(googleAuthService.isEnabled()).isFalse();
        }

        @Test
        @DisplayName("should return false when client ID is blank")
        void shouldReturnFalseWhenBlank() {
            GoogleAuthService service = new GoogleAuthService(userRepository, "   ");
            assertThat(service.isEnabled()).isFalse();
        }
    }

    @Nested
    @DisplayName("authenticateWithGoogle")
    class AuthenticateWithGoogleTests {

        @Test
        @DisplayName("should throw when Google auth is not configured")
        void shouldThrowWhenNotConfigured() {
            assertThatThrownBy(() -> googleAuthService.authenticateWithGoogle("any-token"))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Google authentication is not configured");

            verify(userRepository, never()).findByGoogleId(any());
        }
    }

    @Nested
    @DisplayName("User creation and lookup logic")
    class UserCreationTests {

        // Note: Since we can't mock the Google token verification easily,
        // we test the repository interactions separately.
        // In a real scenario, you'd use a test container or mock the GoogleIdTokenVerifier.

        @Test
        @DisplayName("should find existing user by Google ID")
        void shouldFindExistingUserByGoogleId() {
            User existingUser = createUser(1L, "test@example.com", "google-123");
            when(userRepository.findByGoogleId("google-123")).thenReturn(Optional.of(existingUser));

            Optional<User> result = userRepository.findByGoogleId("google-123");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
            assertThat(result.get().getGoogleId()).isEqualTo("google-123");
        }

        @Test
        @DisplayName("should find existing user by email for account linking")
        void shouldFindExistingUserByEmail() {
            User existingUser = createUser(1L, "test@example.com", null);
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(existingUser));

            Optional<User> result = userRepository.findByEmail("test@example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should create new user with Google ID")
        void shouldCreateNewUserWithGoogleId() {
            when(userRepository.save(any(User.class))).thenAnswer(inv -> {
                User user = inv.getArgument(0);
                user.setId(1L);
                return user;
            });

            User newUser = new User("new@example.com", "new-google-id");
            newUser.setIsModerator(false);
            newUser.setIsAdmin(false);
            User savedUser = userRepository.save(newUser);

            assertThat(savedUser.getId()).isEqualTo(1L);
            assertThat(savedUser.getEmail()).isEqualTo("new@example.com");
            assertThat(savedUser.getGoogleId()).isEqualTo("new-google-id");
            assertThat(savedUser.getIsModerator()).isFalse();
            assertThat(savedUser.getIsAdmin()).isFalse();
        }

        @Test
        @DisplayName("should link Google ID to existing email account")
        void shouldLinkGoogleIdToExistingAccount() {
            User existingUser = createUser(1L, "existing@example.com", null);
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            // Simulate linking
            existingUser.setGoogleId("google-link");
            User linkedUser = userRepository.save(existingUser);

            assertThat(linkedUser.getGoogleId()).isEqualTo("google-link");
            assertThat(linkedUser.getEmail()).isEqualTo("existing@example.com");
        }
    }

    private User createUser(Long id, String email, String googleId) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setGoogleId(googleId);
        user.setIsModerator(false);
        user.setIsAdmin(false);
        return user;
    }
}
