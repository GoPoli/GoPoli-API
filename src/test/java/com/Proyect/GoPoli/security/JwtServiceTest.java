package com.Proyect.GoPoli.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.proyect.gopoli.security.JwtService;

class JwtServiceTest {

    private static final String SECRET = "GoPoliTestSecretKeyMinimo32Chars!!";
    private static final long EXPIRATION_HOURS = 1L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_HOURS);
    }

    @Test
    void generateToken_returnsNonEmptyJwt() {
        String token = jwtService.generateToken(42);

        assertNotNull(token);
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    void parseUserId_returnsSubjectFromValidBearerToken() {
        String token = jwtService.generateToken(99);

        Integer userId = jwtService.parseUserId("Bearer " + token);

        assertEquals(99, userId);
    }

    @Test
    void parseUserId_returnsNullWhenHeaderIsNull() {
        assertNull(jwtService.parseUserId(null));
    }

    @Test
    void parseUserId_returnsNullWhenHeaderLacksBearerPrefix() {
        String token = jwtService.generateToken(1);

        assertNull(jwtService.parseUserId(token));
        assertNull(jwtService.parseUserId("Token " + token));
    }

    @Test
    void parseUserId_returnsNullWhenBearerTokenIsEmpty() {
        assertNull(jwtService.parseUserId("Bearer "));
        assertNull(jwtService.parseUserId("Bearer    "));
    }

    @Test
    void parseUserId_returnsNullForMalformedToken() {
        assertNull(jwtService.parseUserId("Bearer not-a-jwt"));
    }

    @Test
    void parseUserId_returnsNullWhenSignedWithDifferentSecret() {
        String token = jwtService.generateToken(7);
        JwtService other = new JwtService("OtroSecretDistintoDeAlMenos32Chars!", EXPIRATION_HOURS);

        assertNull(other.parseUserId("Bearer " + token));
    }
}
