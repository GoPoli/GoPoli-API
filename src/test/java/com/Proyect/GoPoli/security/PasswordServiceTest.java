package com.Proyect.GoPoli.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.proyect.gopoli.security.PasswordService;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void hash_returnsBcryptPrefixDifferentFromPlaintext() {
        String hashed = passwordService.hash("secret123");

        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$") || hashed.startsWith("$2y$"));
        assertNotEquals("secret123", hashed);
    }

    @Test
    void matches_acceptsRawAgainstItsOwnHash() {
        String hashed = passwordService.hash("miClave");

        assertTrue(passwordService.matches("miClave", hashed));
    }

    @Test
    void matches_rejectsWrongPasswordAgainstHash() {
        String hashed = passwordService.hash("miClave");

        assertFalse(passwordService.matches("otraClave", hashed));
    }

    @Test
    void matches_acceptsLegacyPlaintextEquality() {
        assertTrue(passwordService.matches("legado", "legado"));
    }

    @Test
    void matches_rejectsLegacyPlaintextMismatch() {
        assertFalse(passwordService.matches("legado", "otro"));
    }

    @Test
    void matches_returnsFalseWhenRawIsNull() {
        assertFalse(passwordService.matches(null, "anything"));
    }

    @Test
    void matches_returnsFalseWhenStoredIsNull() {
        assertFalse(passwordService.matches("anything", null));
    }

    @Test
    void isHashed_detectsCommonBcryptPrefixes() {
        assertTrue(passwordService.isHashed("$2a$10$abcdefghijklmnopqrstuu"));
        assertTrue(passwordService.isHashed("$2b$10$abcdefghijklmnopqrstuu"));
        assertTrue(passwordService.isHashed("$2y$10$abcdefghijklmnopqrstuu"));
    }

    @Test
    void isHashed_rejectsPlaintextAndNull() {
        assertFalse(passwordService.isHashed("plaintext"));
        assertFalse(passwordService.isHashed(null));
        assertFalse(passwordService.isHashed("$1$notbcrypt"));
    }
}
