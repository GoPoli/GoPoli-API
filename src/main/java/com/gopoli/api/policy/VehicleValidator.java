package com.gopoli.api.policy;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

public final class VehicleValidator {

    private static final Pattern BRAND = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,}$");
    private static final Pattern MODEL = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 ]{2,}$");
    private static final Pattern COLOR = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{3,}$");
    private static final Pattern PLATE = Pattern.compile("^[A-Za-z0-9]{5,8}$");

    private VehicleValidator() {
    }

    public static Map<String, String> validate(String brand, String model, String color, String plate) {
        Map<String, String> errors = new LinkedHashMap<>();

        if (!BRAND.matcher(trim(brand)).matches()) {
            errors.put("brand", "Marca inválida (mín. 2 letras, solo letras y espacios)");
        }
        if (!MODEL.matcher(trim(model)).matches()) {
            errors.put("model", "Modelo inválido (mín. 2 caracteres, letras, números y espacios)");
        }
        if (!COLOR.matcher(trim(color)).matches()) {
            errors.put("color", "Color inválido (mín. 3 letras, solo letras y espacios)");
        }
        if (!PLATE.matcher(normalizePlate(plate)).matches()) {
            errors.put("plate", "Placa inválida (5-8 caracteres alfanuméricos)");
        }
        return errors;
    }

    public static String normalizePlate(String plate) {
        return trim(plate).toUpperCase(Locale.ROOT);
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
