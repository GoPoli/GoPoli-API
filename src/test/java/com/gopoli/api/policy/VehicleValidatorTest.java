package com.gopoli.api.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

class VehicleValidatorTest {

    @Test
    void validate_returnsEmptyMapForValidVehicle() {
        assertTrue(VehicleValidator.validate("Toyota", "Corolla", "Rojo", "ABC123").isEmpty());
    }

    @Test
    void validate_rejectsInvalidBrand() {
        Map<String, String> errors = VehicleValidator.validate("A", "Corolla", "Rojo", "ABC123");

        assertEquals("Marca inválida (mín. 2 letras, solo letras y espacios)", errors.get("brand"));
    }

    @Test
    void validate_rejectsBrandWithDigits() {
        assertTrue(VehicleValidator.validate("Toyota2", "Corolla", "Rojo", "ABC123").containsKey("brand"));
    }

    @Test
    void validate_rejectsInvalidModel() {
        Map<String, String> errors = VehicleValidator.validate("Toyota", "X", "Rojo", "ABC123");

        assertEquals("Modelo inválido (mín. 2 caracteres, letras, números y espacios)", errors.get("model"));
    }

    @Test
    void validate_rejectsInvalidColor() {
        Map<String, String> errors = VehicleValidator.validate("Toyota", "Corolla", "Ro", "ABC123");

        assertEquals("Color inválido (mín. 3 letras, solo letras y espacios)", errors.get("color"));
    }

    @Test
    void validate_rejectsInvalidPlate() {
        Map<String, String> errors = VehicleValidator.validate("Toyota", "Corolla", "Rojo", "AB");

        assertEquals("Placa inválida (5-8 caracteres alfanuméricos)", errors.get("plate"));
    }

    @Test
    void validate_treatsNullFieldsAsInvalid() {
        Map<String, String> errors = VehicleValidator.validate(null, null, null, null);

        assertEquals(4, errors.size());
        assertTrue(errors.keySet().containsAll(java.util.List.of("brand", "model", "color", "plate")));
    }

    @Test
    void validate_trimsWhitespaceBeforeChecking() {
        assertTrue(VehicleValidator.validate("  Toyota  ", "  Corolla  ", "  Rojo  ", "  abc123  ").isEmpty());
    }

    @Test
    void validate_acceptsAccentedLetters() {
        Map<String, String> errors = VehicleValidator.validate("Nissan", "Versión", "Azúl", "XYZ987");

        assertFalse(errors.containsKey("brand"));
        assertFalse(errors.containsKey("model"));
        assertFalse(errors.containsKey("color"));
    }

    @Test
    void normalizePlate_uppercasesAndTrims() {
        assertEquals("ABC123", VehicleValidator.normalizePlate("  abc123  "));
        assertEquals("", VehicleValidator.normalizePlate(null));
    }
}
