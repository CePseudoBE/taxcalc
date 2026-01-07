package be.hoffmann.backtaxes.security;

import be.hoffmann.backtaxes.config.TokenAuthenticationFilter;
import be.hoffmann.backtaxes.entity.AccessToken;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests de securite pour l'authentification par token.
 */
@ExtendWith(MockitoExtension.class)
class SecurityIntegrationTest {

    @Mock
    private TokenService tokenService;

    private TokenAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new TokenAuthenticationFilter(tokenService);
        SecurityContextHolder.clearContext();
    }

    @Nested
    @DisplayName("Token Authentication")
    class TokenAuthenticationTests {

        @Test
        @DisplayName("should authenticate valid token")
        void shouldAuthenticateValidToken() throws Exception {
            User user = createUser(1L, "test@example.com", false, false);
            when(tokenService.validateToken("valid-token")).thenReturn(Optional.of(user));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer valid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertNotNull(SecurityContextHolder.getContext().getAuthentication());
            assertEquals(user, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
            assertTrue(SecurityContextHolder.getContext().getAuthentication().isAuthenticated());
        }

        @Test
        @DisplayName("should not authenticate invalid token")
        void shouldNotAuthenticateInvalidToken() throws Exception {
            when(tokenService.validateToken("invalid-token")).thenReturn(Optional.empty());

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer invalid-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("should not authenticate expired token")
        void shouldNotAuthenticateExpiredToken() throws Exception {
            when(tokenService.validateToken("expired-token")).thenReturn(Optional.empty());

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer expired-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
        }

        @Test
        @DisplayName("should not authenticate without Authorization header")
        void shouldNotAuthenticateWithoutHeader() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(tokenService, never()).validateToken(anyString());
        }

        @Test
        @DisplayName("should not authenticate with malformed Authorization header")
        void shouldNotAuthenticateWithMalformedHeader() throws Exception {
            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Basic abc123");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            assertNull(SecurityContextHolder.getContext().getAuthentication());
            verify(tokenService, never()).validateToken(anyString());
        }
    }

    @Nested
    @DisplayName("Role-based Authentication")
    class RoleBasedAuthenticationTests {

        @Test
        @DisplayName("should grant ROLE_USER for regular user")
        void shouldGrantRoleUserForRegularUser() throws Exception {
            User user = createUser(1L, "user@example.com", false, false);
            when(tokenService.validateToken("user-token")).thenReturn(Optional.of(user));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer user-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
            assertFalse(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR")));
            assertFalse(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("should grant ROLE_MODERATOR for moderator")
        void shouldGrantRoleModeratorForModerator() throws Exception {
            User user = createUser(2L, "mod@example.com", true, false);
            when(tokenService.validateToken("mod-token")).thenReturn(Optional.of(user));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer mod-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR")));
            assertFalse(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("should grant ROLE_ADMIN for admin")
        void shouldGrantRoleAdminForAdmin() throws Exception {
            User user = createUser(3L, "admin@example.com", false, true);
            when(tokenService.validateToken("admin-token")).thenReturn(Optional.of(user));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer admin-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }

        @Test
        @DisplayName("should grant all roles for admin moderator")
        void shouldGrantAllRolesForAdminModerator() throws Exception {
            User user = createUser(4L, "superuser@example.com", true, true);
            when(tokenService.validateToken("super-token")).thenReturn(Optional.of(user));

            MockHttpServletRequest request = new MockHttpServletRequest();
            request.addHeader("Authorization", "Bearer super-token");
            MockHttpServletResponse response = new MockHttpServletResponse();
            MockFilterChain filterChain = new MockFilterChain();

            filter.doFilter(request, response, filterChain);

            var auth = SecurityContextHolder.getContext().getAuthentication();
            assertNotNull(auth);
            assertEquals(3, auth.getAuthorities().size());
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_MODERATOR")));
            assertTrue(auth.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
        }
    }

    // Helper methods
    private User createUser(Long id, String email, boolean isModerator, boolean isAdmin) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setPasswordHash("hashed");
        user.setIsModerator(isModerator);
        user.setIsAdmin(isAdmin);
        return user;
    }
}
