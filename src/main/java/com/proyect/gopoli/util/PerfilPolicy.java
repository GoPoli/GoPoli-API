package com.proyect.gopoli.util;

import java.util.Locale;

public final class PerfilPolicy {

    private PerfilPolicy() {
    }

    public static String errorCorreo(String correo) {
        String normalizado = normalizarCorreo(correo);
        if (normalizado == null || !normalizado.endsWith("@elpoli.edu.co")) {
            return "Usa tu correo @elpoli.edu.co";
        }
        return null;
    }

    public static String normalizarCorreo(String correo) {
        return correo == null ? null : correo.trim().toLowerCase(Locale.ROOT);
    }

    public static String errorNombre(String nombre) {
        int longitud = nombre == null ? 0 : nombre.trim().length();
        if (longitud < 3 || longitud > 120) {
            return "El nombre debe tener entre 3 y 120 caracteres";
        }
        return null;
    }

    public static String errorTelefono(String telefono) {
        if (telefono == null || !telefono.matches("\\d{8,14}")) {
            return "El teléfono debe contener entre 8 y 14 dígitos";
        }
        return null;
    }
}
