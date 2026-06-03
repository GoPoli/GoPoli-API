package com.proyect.gopoli.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

public final class VehicleValidator {

    private static final Pattern MARCA = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{2,}$");
    private static final Pattern MODELO = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ0-9 ]{2,}$");
    private static final Pattern COLOR = Pattern.compile("^[A-Za-zÁÉÍÓÚáéíóúÑñ ]{3,}$");
    private static final Pattern PLACA = Pattern.compile("^[A-Za-z0-9]{5,8}$");

    private VehicleValidator() {}

    public static Map<String, String> validate(String marca, String modelo, String color, String placa) {
        Map<String, String> errors = new LinkedHashMap<>();

        String m = trim(marca);
        if (m.isEmpty() || !MARCA.matcher(m).matches()) {
            errors.put("marca", "Marca inválida (mín. 2 letras, solo letras y espacios)");
        }

        String mod = trim(modelo);
        if (mod.isEmpty() || !MODELO.matcher(mod).matches()) {
            errors.put("modelo", "Modelo inválido (mín. 2 caracteres, letras, números y espacios)");
        }

        String c = trim(color);
        if (c.isEmpty() || !COLOR.matcher(c).matches()) {
            errors.put("color", "Color inválido (mín. 3 letras, solo letras y espacios)");
        }

        String p = trim(placa).toUpperCase();
        if (p.isEmpty() || !PLACA.matcher(p).matches()) {
            errors.put("placa", "Placa inválida (5-8 caracteres alfanuméricos)");
        }

        return errors;
    }

    public static String normalizePlaca(String placa) {
        return trim(placa).toUpperCase();
    }

    private static String trim(String s) {
        return s == null ? "" : s.trim();
    }
}
