package com.Proyect.GoPoli.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

import com.proyect.gopoli.util.PerfilPolicy;

class PerfilPolicyTest {

    @Test
    void rechazaCambioAUnCorreoNoInstitucional() {
        assertEquals("Usa tu correo @elpoli.edu.co",
                PerfilPolicy.errorCorreo("persona@gmail.com"));
    }

    @Test
    void normalizaYAdmiteCorreoInstitucional() {
        assertNull(PerfilPolicy.errorCorreo("  Estudiante@ELPOLI.EDU.CO "));
        assertEquals("estudiante@elpoli.edu.co",
                PerfilPolicy.normalizarCorreo("  Estudiante@ELPOLI.EDU.CO "));
    }

    @Test
    void limitaNombreYTelefonoAValoresValidos() {
        assertEquals("El nombre debe tener entre 3 y 120 caracteres",
                PerfilPolicy.errorNombre("  A "));
        assertEquals("El teléfono debe contener entre 8 y 14 dígitos",
                PerfilPolicy.errorTelefono("12-34"));
        assertNull(PerfilPolicy.errorTelefono("3001234567"));
    }
}
