package be.hoffmann.backtaxes.controller;

import be.hoffmann.backtaxes.dto.response.UserResponse;
import be.hoffmann.backtaxes.entity.AccessToken;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.exception.GlobalExceptionHandler;
import be.hoffmann.backtaxes.exception.ValidationException;
import be.hoffmann.backtaxes.service.GoogleAuthService;
import be.hoffmann.backtaxes.service.TokenService;
import be.hoffmann.backtaxes.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @Mock
    private TokenService tokenService;

    @Mock
    private GoogleAuthService googleAuthService;

    @BeforeEach
    void setUp() {
        UserController controller = new UserController(userService, tokenService, googleAuthService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Nested
    @DisplayName("POST /api/auth/google")
    class GoogleAuthTests {

        @Test
        @DisplayName("should authenticate with Google and return token")
        void shouldAuthenticateWithGoogleAndReturnToken() throws Exception {
            User user = createUser(1L, "test@example.com", "google-123");
            AccessToken token = createToken("generated-token-123", user);
            UserResponse userResponse = new UserResponse(1L, "test@example.com", false, false, Instant.now());

            when(googleAuthService.authenticateWithGoogle("valid-google-id-token")).thenReturn(user);
            when(tokenService.generateToken(user, "nuxt-bff")).thenReturn(token);
            when(tokenService.getTokenExpirationSeconds()).thenReturn(86400L);
            when(userService.toResponse(user)).thenReturn(userResponse);

            mockMvc.perform(post("/api/auth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .header("X-Client-Name", "nuxt-bff")
                            .content("""
                                {
                                    "idToken": "valid-google-id-token"
                                }
                                """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.user.id", is(1)))
                    .andExpect(jsonPath("$.data.user.email", is("test@example.com")))
                    .andExpect(jsonPath("$.data.accessToken", is("generated-token-123")))
                    .andExpect(jsonPath("$.data.expiresIn", is(86400)))
                    .andExpect(jsonPath("$.message", containsString("Google login successful")));

            verify(googleAuthService).authenticateWithGoogle("valid-google-id-token");
            verify(tokenService).generateToken(user, "nuxt-bff");
        }

        @Test
        @DisplayName("should return 400 when Google token is invalid")
        void shouldReturn400WhenGoogleTokenInvalid() throws Exception {
            when(googleAuthService.authenticateWithGoogle("invalid-token"))
                    .thenThrow(new ValidationException("Invalid Google ID token"));

            mockMvc.perform(post("/api/auth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "idToken": "invalid-token"
                                }
                                """))
                    .andExpect(status().isBadRequest());

            verify(tokenService, never()).generateToken(any(), anyString());
        }

        @Test
        @DisplayName("should use default client name when header missing")
        void shouldUseDefaultClientName() throws Exception {
            User user = createUser(1L, "test@example.com", "google-123");
            AccessToken token = createToken("token", user);
            UserResponse userResponse = new UserResponse(1L, "test@example.com", false, false, Instant.now());

            when(googleAuthService.authenticateWithGoogle("valid-token")).thenReturn(user);
            when(tokenService.generateToken(user, "unknown")).thenReturn(token);
            when(tokenService.getTokenExpirationSeconds()).thenReturn(86400L);
            when(userService.toResponse(user)).thenReturn(userResponse);

            mockMvc.perform(post("/api/auth/google")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                {
                                    "idToken": "valid-token"
                                }
                                """))
                    .andExpect(status().isOk());

            verify(tokenService).generateToken(user, "unknown");
        }
    }

    @Nested
    @DisplayName("POST /api/auth/logout")
    class LogoutTests {

        @Test
        @DisplayName("should revoke token on logout")
        void shouldRevokeToken() throws Exception {
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer my-token-to-revoke"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", containsString("Logout successful")));

            verify(tokenService).revokeToken("my-token-to-revoke");
        }

        @Test
        @DisplayName("should succeed even without authorization header")
        void shouldSucceedWithoutAuthHeader() throws Exception {
            mockMvc.perform(post("/api/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", containsString("Logout successful")));

            verify(tokenService, never()).revokeToken(anyString());
        }
    }

    @Nested
    @DisplayName("GET /api/auth/check")
    class CheckAuthTests {

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            // Sans authentification, le SecurityContext doit etre vide
            SecurityContextHolder.clearContext();
            mockMvc.perform(get("/api/auth/check"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", containsString("Not authenticated")));
        }
    }

    @Nested
    @DisplayName("GET /api/users/me")
    class GetCurrentUserTests {

        @Test
        @DisplayName("should return 401 when not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            SecurityContextHolder.clearContext();
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", containsString("Not authenticated")));
        }
    }

    private User createUser(Long id, String email, String googleId) {
        User user = new User(email, googleId);
        user.setId(id);
        user.setIsModerator(false);
        user.setIsAdmin(false);
        return user;
    }

    private AccessToken createToken(String tokenValue, User user) {
        AccessToken token = new AccessToken(
                tokenValue,
                user,
                "test-client",
                Instant.now().plus(24, ChronoUnit.HOURS)
        );
        ReflectionTestUtils.setField(token, "id", 1L);
        return token;
    }
}
