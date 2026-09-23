package com.Proyect.GoPoli.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

import com.proyect.gopoli.util.VehicleValidator;

class VehicleValidatorTest {

    @Test
    void validate_returnsEmptyMapForValidVehicle() {
        Map<String, String> errors = VehicleValidator.validate("Toyota", "Corolla", "Rojo", "ABC123");

        assertTrue(errors.isEmpty());
    }

    @Test
    void validate_rejectsInvalidMarca() {
        Map<String, String> errors = VehicleValidator.validate("A", "Corolla", "Rojo", "ABC123");

        assertTrue(errors.containsKey("marca"));
        assertEquals("Marca inválida (mín. 2 letras, solo letras y espacios)", errors.get("marca"));
    }

    @Test
    void validate_rejectsMarcaWithDigits() {
        Map<String, String> errors = VehicleValidator.validate("Toyota2", "Corolla", "Rojo", "ABC123");

        assertTrue(errors.containsKey("marca"));
    }

    @Test
    void validate_rejectsInvalidModelo() {
        Map<String, String> errors = VehicleValidator.validate("Toyota", "X", "Rojo", "ABC123");

        assertTrue(errors.containsKey("modelo"));
        assertEquals("Modelo inválido (mín. 2 caracteres, letras, números y espacios)", errors.get("modelo"));
    }

    @Test
    void validate_rejectsInvalidColor() {
        Map<String, String> errors = VehicleValidator.validate("Toyota", "Corolla", "Ro", "ABC123");

        assertTrue(errors.containsKey("color"));
        assertEquals("Color inválido (mín. 3 letras, solo letras y espacios)", errors.get("color"));
    }

    @Test
    void validate_rejectsInvalidPlaca() {
        Map<String, String> errors = VehicleValidator.validate("Toyota", "Corolla", "Rojo", "AB");

        assertTrue(errors.containsKey("placa"));
        assertEquals("Placa inválida (5-8 caracteres alfanuméricos)", errors.get("placa"));
    }

    @Test
    void validate_treatsNullFieldsAsInvalid() {
        Map<String, String> errors = VehicleValidator.validate(null, null, null, null);

        assertEquals(4, errors.size());
        assertTrue(errors.containsKey("marca"));
        assertTrue(errors.containsKey("modelo"));
        assertTrue(errors.containsKey("color"));
        assertTrue(errors.containsKey("placa"));
    }

    @Test
    void validate_trimsWhitespaceBeforeChecking() {
        Map<String, String> errors = VehicleValidator.validate("  Toyota  ", "  Corolla  ", "  Rojo  ", "  abc123  ");

        assertTrue(errors.isEmpty());
    }

    @Test
    void validate_acceptsAccentedLettersInMarcaModeloColor() {
        Map<String, String> errors = VehicleValidator.validate("Nissan", "Versión", "Azúl", "XYZ987");

        assertFalse(errors.containsKey("marca"));
        assertFalse(errors.containsKey("modelo"));
        assertFalse(errors.containsKey("color"));
    }

    @Test
    void normalizePlaca_uppercasesAndTrims() {
        assertEquals("ABC123", VehicleValidator.normalizePlaca("  abc123  "));
    }

    @Test
    void normalizePlaca_returnsEmptyForNull() {
        assertEquals("", VehicleValidator.normalizePlaca(null));
    }
}
