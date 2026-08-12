package com.greentrack.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private JwtUtil jwtUtil;
    private UserDetails user;

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "jwtSecret", "test-secret-key-that-is-long-enough-for-hmac-sha-256-signing");
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", 3600_000L);
        ReflectionTestUtils.setField(jwtUtil, "refreshExpirationMs", 604_800_000L);
        user = org.springframework.security.core.userdetails.User
                .withUsername("ama@greentrack.app").password("x").authorities("ROLE_CITIZEN").build();
    }

    @Test
    void accessToken_isValidForMatchingUser() {
        String token = jwtUtil.generateToken(user);
        assertThat(jwtUtil.isTokenValid(token, user)).isTrue();
        assertThat(jwtUtil.extractUsername(token)).isEqualTo("ama@greentrack.app");
    }

    // A refresh token must not be usable to pass isTokenValid — otherwise it can
    // stand in for an access token on any authenticated endpoint indefinitely.
    @Test
    void refreshToken_isNotAcceptedAsAccessToken() {
        String refreshToken = jwtUtil.generateRefreshToken(user);
        assertThat(jwtUtil.isRefreshToken(refreshToken)).isTrue();
        assertThat(jwtUtil.isTokenValid(refreshToken, user)).isFalse();
    }

    @Test
    void accessToken_isNotAcceptedAsRefreshToken() {
        String accessToken = jwtUtil.generateToken(user);
        assertThat(jwtUtil.isRefreshToken(accessToken)).isFalse();
    }

    // jjwt throws ExpiredJwtException while parsing rather than returning a flag,
    // so isTokenValid()'s internal try/catch is what actually protects callers —
    // this confirms an expired token never validates.
    @Test
    void expiredToken_isRejected() {
        ReflectionTestUtils.setField(jwtUtil, "jwtExpirationMs", -1000L);
        String token = jwtUtil.generateToken(user);
        assertThat(jwtUtil.isTokenValid(token, user)).isFalse();
    }
}
