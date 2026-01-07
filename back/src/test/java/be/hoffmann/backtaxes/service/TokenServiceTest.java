package be.hoffmann.backtaxes.service;

import be.hoffmann.backtaxes.entity.AccessToken;
import be.hoffmann.backtaxes.entity.User;
import be.hoffmann.backtaxes.repository.AccessTokenRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private AccessTokenRepository tokenRepository;

    @Captor
    private ArgumentCaptor<AccessToken> tokenCaptor;

    private TokenService tokenService;

    @BeforeEach
    void setUp() {
        tokenService = new TokenService(tokenRepository);
        // Configuration par defaut
        ReflectionTestUtils.setField(tokenService, "tokenExpirationHours", 24);
        ReflectionTestUtils.setField(tokenService, "tokenLength", 32);
    }

    @Nested
    @DisplayName("generateToken")
    class GenerateTokenTests {

        @Test
        @DisplayName("should generate token with correct properties")
        void shouldGenerateTokenWithCorrectProperties() {
            User user = createUser(1L, "test@example.com");

            when(tokenRepository.save(any(AccessToken.class))).thenAnswer(invocation -> {
                AccessToken token = invocation.getArgument(0);
                // Simuler l'ID genere par la DB
                ReflectionTestUtils.setField(token, "id", 1L);
                return token;
            });

            AccessToken result = tokenService.generateToken(user, "nuxt-bff");

            assertThat(result).isNotNull();
            assertThat(result.getToken()).isNotBlank();
            assertThat(result.getToken().length()).isGreaterThan(20); // Base64 de 32 bytes
            assertThat(result.getUser()).isEqualTo(user);
            assertThat(result.getClientName()).isEqualTo("nuxt-bff");
            assertThat(result.getExpiresAt()).isAfter(Instant.now());
            assertThat(result.getExpiresAt()).isBefore(Instant.now().plus(25, ChronoUnit.HOURS));

            verify(tokenRepository).save(tokenCaptor.capture());
            AccessToken savedToken = tokenCaptor.getValue();
            assertThat(savedToken.getUser().getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should generate unique tokens for same user")
        void shouldGenerateUniqueTokens() {
            User user = createUser(1L, "test@example.com");

            when(tokenRepository.save(any(AccessToken.class))).thenAnswer(inv -> inv.getArgument(0));

            AccessToken token1 = tokenService.generateToken(user, "client1");
            AccessToken token2 = tokenService.generateToken(user, "client2");

            assertThat(token1.getToken()).isNotEqualTo(token2.getToken());
        }
    }

    @Nested
    @DisplayName("validateToken")
    class ValidateTokenTests {

        @Test
        @DisplayName("should return user when token is valid and not expired")
        void shouldReturnUserWhenTokenValid() {
            User user = createUser(1L, "test@example.com");
            AccessToken token = createToken("valid-token", user, Instant.now().plus(1, ChronoUnit.HOURS));

            when(tokenRepository.findByToken("valid-token")).thenReturn(Optional.of(token));
            when(tokenRepository.save(any(AccessToken.class))).thenAnswer(inv -> inv.getArgument(0));

            Optional<User> result = tokenService.validateToken("valid-token");

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(1L);
            assertThat(result.get().getEmail()).isEqualTo("test@example.com");

            // Verifie que lastUsedAt est mis a jour
            verify(tokenRepository).save(tokenCaptor.capture());
            assertThat(tokenCaptor.getValue().getLastUsedAt()).isNotNull();
        }

        @Test
        @DisplayName("should return empty when token not found")
        void shouldReturnEmptyWhenTokenNotFound() {
            when(tokenRepository.findByToken("unknown-token")).thenReturn(Optional.empty());

            Optional<User> result = tokenService.validateToken("unknown-token");

            assertThat(result).isEmpty();
            verify(tokenRepository, never()).save(any());
        }

        @Test
        @DisplayName("should return empty when token is expired")
        void shouldReturnEmptyWhenTokenExpired() {
            User user = createUser(1L, "test@example.com");
            AccessToken expiredToken = createToken("expired-token", user, Instant.now().minus(1, ChronoUnit.HOURS));

            when(tokenRepository.findByToken("expired-token")).thenReturn(Optional.of(expiredToken));

            Optional<User> result = tokenService.validateToken("expired-token");

            assertThat(result).isEmpty();
            verify(tokenRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("revokeToken")
    class RevokeTokenTests {

        @Test
        @DisplayName("should delete token from repository")
        void shouldDeleteToken() {
            tokenService.revokeToken("token-to-revoke");

            verify(tokenRepository).deleteByToken("token-to-revoke");
        }
    }

    @Nested
    @DisplayName("revokeAllUserTokens")
    class RevokeAllUserTokensTests {

        @Test
        @DisplayName("should delete all tokens for user")
        void shouldDeleteAllUserTokens() {
            tokenService.revokeAllUserTokens(1L);

            verify(tokenRepository).deleteAllByUserId(1L);
        }
    }

    @Nested
    @DisplayName("cleanupExpiredTokens")
    class CleanupExpiredTokensTests {

        @Test
        @DisplayName("should call repository to delete expired tokens")
        void shouldCleanupExpiredTokens() {
            when(tokenRepository.deleteExpiredTokens(any(Instant.class))).thenReturn(5);

            tokenService.cleanupExpiredTokens();

            verify(tokenRepository).deleteExpiredTokens(any(Instant.class));
        }
    }

    @Nested
    @DisplayName("getTokenExpirationSeconds")
    class GetTokenExpirationSecondsTests {

        @Test
        @DisplayName("should return correct expiration in seconds")
        void shouldReturnCorrectExpiration() {
            long result = tokenService.getTokenExpirationSeconds();

            // 24 heures = 86400 secondes
            assertThat(result).isEqualTo(86400L);
        }
    }

    private User createUser(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setIsModerator(false);
        return user;
    }

    private AccessToken createToken(String tokenValue, User user, Instant expiresAt) {
        AccessToken token = new AccessToken(tokenValue, user, "test-client", expiresAt);
        ReflectionTestUtils.setField(token, "id", 1L);
        return token;
    }
}
