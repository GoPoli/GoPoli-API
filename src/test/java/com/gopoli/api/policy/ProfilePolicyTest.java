package com.gopoli.api.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class ProfilePolicyTest {

    @Test
    void rejectsNonInstitutionalEmail() {
        assertEquals("Usa tu correo @elpoli.edu.co", ProfilePolicy.emailError("persona@gmail.com"));
    }

    @Test
    void normalizesAndAcceptsInstitutionalEmail() {
        assertNull(ProfilePolicy.emailError("  Estudiante@ELPOLI.EDU.CO "));
        assertEquals("estudiante@elpoli.edu.co", ProfilePolicy.normalizeEmail("  Estudiante@ELPOLI.EDU.CO "));
    }

    @Test
    void limitsNameAndPhoneToValidValues() {
        assertEquals("El nombre debe tener entre 3 y 120 caracteres", ProfilePolicy.nameError("  A "));
        assertEquals("El teléfono debe contener entre 8 y 14 dígitos", ProfilePolicy.phoneError("12-34"));
        assertNull(ProfilePolicy.phoneError("3001234567"));
    }
}
