package com.gopoli.api.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PasswordServiceTest {

    private PasswordService passwordService;

    @BeforeEach
    void setUp() {
        passwordService = new PasswordService();
    }

    @Test
    void hash_returnsBcryptPrefixDifferentFromPlaintext() {
        String hashed = passwordService.hash("secret123");

        assertTrue(passwordService.isHashed(hashed));
        assertNotEquals("secret123", hashed);
    }

    @Test
    void matches_acceptsRawAgainstItsOwnHash() {
        assertTrue(passwordService.matches("myPassword", passwordService.hash("myPassword")));
    }

    @Test
    void matches_rejectsWrongPasswordAgainstHash() {
        assertFalse(passwordService.matches("otherPassword", passwordService.hash("myPassword")));
    }

    @Test
    void matches_rejectsPlaintextStoredValues() {
        assertFalse(passwordService.matches("plain", "plain"));
    }

    @Test
    void matches_returnsFalseWhenAnyValueIsNull() {
        assertFalse(passwordService.matches(null, "anything"));
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
