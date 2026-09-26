package com.gopoli.api.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class JwtServiceTest {

    private static final String SECRET = "GoPoliTestSecretKeyMinimum32Chars!!";
    private static final long EXPIRATION_HOURS = 1L;

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_HOURS);
    }

    @Test
    void constructor_rejectsShortSecret() {
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> new JwtService("too-short", EXPIRATION_HOURS));
        assertEquals("GOPOLI_JWT_SECRET debe tener al menos 32 caracteres", error.getMessage());
    }

    @Test
    void generateToken_returnsThreePartJwt() {
        String token = jwtService.generateToken(42);

        assertNotNull(token);
        assertEquals(3, token.split("\\.").length);
    }

    @Test
    void parseUserId_returnsSubjectFromValidBearerToken() {
        String token = jwtService.generateToken(99);

        assertEquals(99, jwtService.parseUserId("Bearer " + token));
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
        JwtService other = new JwtService("AnotherSecretThatIsAtLeast32Chars!!", EXPIRATION_HOURS);

        assertNull(other.parseUserId("Bearer " + token));
    }
}
