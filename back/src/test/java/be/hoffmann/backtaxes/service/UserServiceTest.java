package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.request.UserRegistrationRequest;
import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, passwordEncoder);
    }

    @Nested
    @DisplayName("register")
    class RegisterTests {

        @Test
        @DisplayName("should register new user successfully")
        void shouldRegisterNewUser() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "test@example.com",
                    "password123"
            );

            when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
            when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                user.setCreatedAt(Instant.now());
                return user;
            });

            User result = userService.register(request);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getIsModerator()).isFalse();

            verify(userRepository).existsByEmail("test@example.com");
            verify(passwordEncoder).encode("password123");
            verify(userRepository).save(userCaptor.capture());

            User savedUser = userCaptor.getValue();
            assertThat(savedUser.getPasswordHash()).isEqualTo("encoded_password");
        }

        @Test
        @DisplayName("should throw ValidationException when email already exists")
        void shouldThrowWhenEmailExists() {
            UserRegistrationRequest request = new UserRegistrationRequest(
                    "existing@example.com",
                    "password123"
            );

            when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(ValidationException.class)
                    .hasMessageContaining("Email already exists");

            verify(userRepository).existsByEmail("existing@example.com");
            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("should return user when found by email")
        void shouldReturnUserWhenFound() {
            User user = createUser(1L, "test@example.com");
            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

            Optional<User> result = userService.findByEmail("test@example.com");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should return empty when user not found")
        void shouldReturnEmptyWhenNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            Optional<User> result = userService.findByEmail("unknown@example.com");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindByIdTests {

        @Test
        @DisplayName("should return user when found")
        void shouldReturnUserWhenFound() {
            User user = createUser(1L, "test@example.com");
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            User result = userService.findById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowWhenNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User")
                    .hasMessageContaining("999");
        }
    }

    @Nested
    @DisplayName("authenticate")
    class AuthenticateTests {

        @Test
        @DisplayName("should return user when credentials are valid")
        void shouldReturnUserWhenCredentialsValid() {
            User user = createUser(1L, "test@example.com");
            user.setPasswordHash("encoded_password");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("password123", "encoded_password")).thenReturn(true);

            Optional<User> result = userService.authenticate("test@example.com", "password123");

            assertThat(result).isPresent();
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("should return empty when email not found")
        void shouldReturnEmptyWhenEmailNotFound() {
            when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

            Optional<User> result = userService.authenticate("unknown@example.com", "password123");

            assertThat(result).isEmpty();
            verify(passwordEncoder, never()).matches(anyString(), anyString());
        }

        @Test
        @DisplayName("should return empty when password does not match")
        void shouldReturnEmptyWhenPasswordNotMatch() {
            User user = createUser(1L, "test@example.com");
            user.setPasswordHash("encoded_password");

            when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));
            when(passwordEncoder.matches("wrongpassword", "encoded_password")).thenReturn(false);

            Optional<User> result = userService.authenticate("test@example.com", "wrongpassword");

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("toResponse")
    class ToResponseTests {

        @Test
        @DisplayName("should convert User to UserResponse")
        void shouldConvertToResponse() {
            Instant createdTime = Instant.parse("2024-01-15T10:30:00Z");
            User user = createUser(1L, "test@example.com");
            user.setIsModerator(true);
            user.setCreatedAt(createdTime);

            UserResponse response = userService.toResponse(user);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.isModerator()).isTrue();
            assertThat(response.createdAt()).isEqualTo(createdTime);
        }
    }

    @Nested
    @DisplayName("setModeratorStatus")
    class SetModeratorStatusTests {

        @Test
        @DisplayName("should set moderator status to true")
        void shouldSetModeratorStatusTrue() {
            User user = createUser(1L, "test@example.com");
            user.setIsModerator(false);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.setModeratorStatus(1L, true);

            assertThat(result.getIsModerator()).isTrue();
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsModerator()).isTrue();
        }

        @Test
        @DisplayName("should set moderator status to false")
        void shouldSetModeratorStatusFalse() {
            User user = createUser(1L, "test@example.com");
            user.setIsModerator(true);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.setModeratorStatus(1L, false);

            assertThat(result.getIsModerator()).isFalse();
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.setModeratorStatus(999L, true))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setIsModerator(false);
        return user;
    }
}
