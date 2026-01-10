package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.ResourceNotFoundException;
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

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests pour UserService.
 * L'authentification se fait uniquement via Google OAuth (voir GoogleAuthServiceTest).
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Captor
    private ArgumentCaptor<User> userCaptor;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Nested
    @DisplayName("findByEmail")
    class FindByEmailTests {

        @Test
        @DisplayName("should return user when found by email")
        void shouldReturnUserWhenFound() {
            User user = createUser(1L, "test@example.com", "google-123");
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
            User user = createUser(1L, "test@example.com", "google-123");
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
    @DisplayName("toResponse")
    class ToResponseTests {

        @Test
        @DisplayName("should convert User to UserResponse")
        void shouldConvertToResponse() {
            Instant createdTime = Instant.parse("2024-01-15T10:30:00Z");
            User user = createUser(1L, "test@example.com", "google-123");
            user.setIsModerator(true);
            user.setIsAdmin(false);
            user.setCreatedAt(createdTime);

            UserResponse response = userService.toResponse(user);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("test@example.com");
            assertThat(response.isModerator()).isTrue();
            assertThat(response.isAdmin()).isFalse();
            assertThat(response.createdAt()).isEqualTo(createdTime);
        }

        @Test
        @DisplayName("should convert admin user to UserResponse")
        void shouldConvertAdminToResponse() {
            Instant createdTime = Instant.parse("2024-01-15T10:30:00Z");
            User user = createUser(1L, "admin@example.com", "google-admin");
            user.setIsModerator(false);
            user.setIsAdmin(true);
            user.setCreatedAt(createdTime);

            UserResponse response = userService.toResponse(user);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.email()).isEqualTo("admin@example.com");
            assertThat(response.isModerator()).isFalse();
            assertThat(response.isAdmin()).isTrue();
        }
    }

    @Nested
    @DisplayName("setModeratorStatus")
    class SetModeratorStatusTests {

        @Test
        @DisplayName("should set moderator status to true")
        void shouldSetModeratorStatusTrue() {
            User user = createUser(1L, "test@example.com", "google-123");
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
            User user = createUser(1L, "test@example.com", "google-123");
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

    @Nested
    @DisplayName("setAdminStatus")
    class SetAdminStatusTests {

        @Test
        @DisplayName("should set admin status to true")
        void shouldSetAdminStatusTrue() {
            User user = createUser(1L, "test@example.com", "google-123");
            user.setIsAdmin(false);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.setAdminStatus(1L, true);

            assertThat(result.getIsAdmin()).isTrue();
            verify(userRepository).save(userCaptor.capture());
            assertThat(userCaptor.getValue().getIsAdmin()).isTrue();
        }

        @Test
        @DisplayName("should set admin status to false")
        void shouldSetAdminStatusFalse() {
            User user = createUser(1L, "test@example.com", "google-123");
            user.setIsAdmin(true);

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

            User result = userService.setAdminStatus(1L, false);

            assertThat(result.getIsAdmin()).isFalse();
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.setAdminStatus(999L, true))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteAccount")
    class DeleteAccountTests {

        @Test
        @DisplayName("should delete user account")
        void shouldDeleteAccount() {
            User user = createUser(1L, "test@example.com", "google-123");

            when(userRepository.findById(1L)).thenReturn(Optional.of(user));

            userService.deleteAccount(1L);

            verify(userRepository).delete(user);
        }

        @Test
        @DisplayName("should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteAccount(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAllTests {

        @Test
        @DisplayName("should return all users")
        void shouldReturnAllUsers() {
            List<User> users = List.of(
                    createUser(1L, "user1@example.com", "google-1"),
                    createUser(2L, "user2@example.com", "google-2")
            );
            when(userRepository.findAll()).thenReturn(users);

            List<User> result = userService.findAll();

            assertThat(result).hasSize(2);
            assertThat(result.get(0).getEmail()).isEqualTo("user1@example.com");
            assertThat(result.get(1).getEmail()).isEqualTo("user2@example.com");
        }

        @Test
        @DisplayName("should return empty list when no users")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userRepository.findAll()).thenReturn(List.of());

            List<User> result = userService.findAll();

            assertThat(result).isEmpty();
        }
    }

    private User createUser(Long id, String email, String googleId) {
        User user = new User(email, googleId);
        user.setId(id);
        user.setIsModerator(false);
        user.setIsAdmin(false);
        return user;
    }
}
